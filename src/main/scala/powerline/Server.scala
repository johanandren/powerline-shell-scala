package powerline

import java.net.InetSocketAddress

import akka.actor._
import akka.io.{IO, Tcp}
import powerline.vcs.RepositySupervisor

class Server(config: AppConfig) extends Actor with ActorLogging {

  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 18888))

  val directoryHistory = context.actorOf(DirectoryHistory.props, "directory-history")
  val repositories = context.actorOf(RepositySupervisor.props, "repos")
  val themes = context.actorOf(ThemeSupervisor.props, "themes")

  def receive = {
    case b@Bound(localAddress) =>
      log.info(s"Server started at $localAddress")

    case CommandFailed(_: Bind) =>
      log.error("Failed to bind server")
      context.system.terminate()

    case c@Connected(remote, local) =>
      val handler = context.actorOf(RequestHandler.props(config, directoryHistory, repositories, themes))
      val connection = sender()
      connection ! Register(handler)
  }

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy(){
    case x: Exception => SupervisorStrategy.stop
  }
}

