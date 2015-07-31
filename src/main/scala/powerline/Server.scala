package powerline

import java.io._

import akka.io.Tcp.{Write, Received, PeerClosed}
import powerline.DirectoryHistory.DirectoryVisited
import powerline.vcs.Repositories

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Failure
import akka.actor._
import akka.io.{ IO, Tcp }
import akka.util.{Timeout, ByteString}
import akka.pattern.{pipe, ask}
import java.net.InetSocketAddress

class Server(config: AppConfig) extends Actor with ActorLogging {

  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 18888))

  val directoryHistory = context.actorOf(DirectoryHistory.props, "directory-history")
  val repositories = context.actorOf(Repositories.props, "repositories")

  def receive = {
    case b@Bound(localAddress) =>
      log.info(s"Server started at $localAddress")

    case CommandFailed(_: Bind) =>
      log.error("Failed to bind server")
      context.system.shutdown()

    case c@Connected(remote, local) =>
      val handler = context.actorOf(Props(new RequestHandler(config, directoryHistory, repositories)))
      val connection = sender()
      connection ! Register(handler)
  }

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy(){
    case x: Exception => SupervisorStrategy.stop
  }
}

