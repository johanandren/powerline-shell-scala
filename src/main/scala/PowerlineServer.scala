import akka.actor.{Props, ActorSystem}
import powerline.{AppConfig, Server, Theme}

/** App main that starts the whole shebang */
object PowerlineServer extends App {

  val system = ActorSystem("main")

  val argList = args.toList
  val debug = argList.contains("-debug")
  val config = AppConfig(debug)

  val server = system.actorOf(Props(new Server(config)), "server")
  system.awaitTermination()
}