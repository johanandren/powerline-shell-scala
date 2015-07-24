import akka.actor.{Props, ActorSystem}
import powerline.{AppConfig, Server, Theme}

/** App main that starts the whole shebang */
object PowerlineServer extends App {

  val argList = args.toList

  val debug = argList.contains("-debug")
  val config = AppConfig(debug = debug, theme = Theme.load("darcula"))

  val system = ActorSystem("main")

  val server = system.actorOf(Props(new Server(config)), "server")
  system.awaitTermination()
}