import powerline.{AppConfig, Server, Theme}

/** App main that starts the whole shebang */
object PowerlineServer extends App {

  val argList = args.toList

  val debug = argList.contains("-debug")
  val config = AppConfig(debug = debug, theme = Theme.load("darcula"))

  try {
    val server = new Server(config)
    server.start()
  } catch {
    case x: Throwable => println("Failed to start server, because: " + x.toString)
  }

}