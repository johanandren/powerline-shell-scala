import com.sun.akuma.Daemon
import powerline.bash.BashPrompt
import powerline.{AppConfig, Server}

/** App main that starts the whole shebang */
object PowerlineServer extends App {

  val config = parseAppConfig(args.toSeq)
  daemonized {
    val server = new Server(config)
    server.start()
  }


  // parse args into an app config
  def parseAppConfig(args: Seq[String]) = {

    val debug = args.contains("-debug")
    val Generator = """-shell=([\w])""".r
    val generator = args.collectFirst {
      case Generator(shell) => shell
    }.fold(BashPrompt) {
      case "bash" => BashPrompt
      case x => throw new RuntimeException(s"Unknown/unsupported shell $x")
    }

    AppConfig(debug, generator)
  }

  def daemonized(block: => Any): Unit = {
    try {
      if (!config.debug) {
        // Daemonize the powerline server
        val daemon = new Daemon()
        if (daemon.isDaemonized) {
          daemon.init()
        } else {
          daemon.daemonize()
          System.exit(0)
        }
      }

      val res = block

    } catch {
      case x: Throwable => println("Failed to start daemon, because: " + x.toString)
    }
  }

}