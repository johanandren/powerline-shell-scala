import akka.actor.{Props, ActorSystem}
import powerline.{AppConfig, Server, Theme}

/** App main that starts the whole shebang */
object PowerlineServer extends App {

  val argList = args.toList
  val system = ActorSystem("main")

  system.log.info("Starting with args {}", argList)

  val debug = argList.contains("-debug")
  val config = AppConfig(debug = debug, theme = Theme.load(argList(0)))


  val server = system.actorOf(Props(new Server(config)), "server")
  system.awaitTermination()
}