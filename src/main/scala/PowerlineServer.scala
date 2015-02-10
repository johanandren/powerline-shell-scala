import com.sun.akuma.Daemon
import powerline.shells.{BashPrompt, ZshPrompt}
import powerline.{AppConfig, Server, Theme}

/** App main that starts the whole shebang */
object PowerlineServer extends App {

  val argList = args.toList

  val debug = argList.contains("-debug")

  val config = AppConfig(
    debug = debug,
    theme = Theme.load("darcula"))


  daemonized {
    val server = new Server(config)
    server.start()
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