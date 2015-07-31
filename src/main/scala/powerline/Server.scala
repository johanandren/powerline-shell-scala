package powerline

import java.io._

import akka.io.Tcp.{Write, Received, PeerClosed}
import powerline.DirectoryHistory.DirectoryVisited

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

  def receive = {
    case b@Bound(localAddress) =>
      log.info(s"Server started at $localAddress")

    case CommandFailed(_: Bind) =>
      log.error("Failed to bind server")
      context.system.shutdown()

    case c@Connected(remote, local) =>
      val handler = context.actorOf(Props(new RequestHandler(config, directoryHistory)))
      val connection = sender()
      connection ! Register(handler)
  }

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy(){
    case x: Exception => SupervisorStrategy.stop
  }
}

class RequestHandler(config: AppConfig, directoryHistory: ActorRef) extends Actor with ActorLogging {

  import context.dispatcher

  case class Response(recipient: ActorRef, response: ByteString)

  def receive = {
    case Received(data) =>
      val recipient = sender()
      (parseRequest(data) match {
        case prompt: PromptRequest => handlePromptRequest(prompt)
        case dir: DirHistorySearchReq => handleDirSearchReq(dir)
      }).map(result => Response(recipient, result))
         .pipeTo(self)


    case Response(recipient, data) =>
      recipient ! Write(data)
      context stop self

    case Failure(error) =>
      log.error(error, "Failed to handle request")
      context stop self

    case PeerClosed     =>
      context stop self
  }


  // implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))

  val generator = new PromptGenerator(config)

  def parseRequest(data: ByteString): Request = {
    data.decodeString("UTF-8").split('\n').toSeq match {
      case "prompt" +: rest => parsePromptRequest(rest)
      case "dirsearch" +: rest => parseDirSearchRequest(rest)
      case wat +: _ => throw new RuntimeException(s"Unknown request type $wat")
      case _ => throw new RuntimeException("Malformed request")
    }
  }


  def handleDirSearchReq(request: DirHistorySearchReq): Future[ByteString] = {
    implicit val timeout = Timeout(2.seconds)
    (directoryHistory ? DirectoryHistory.Query(request.query))
      .mapTo[DirectoryHistory.Result]
      .map { result =>
        ByteString(result.path.fold("")(_.getAbsolutePath))
      }
  }

  def handlePromptRequest(request: PromptRequest): Future[ByteString] = {
    directoryHistory ! DirectoryVisited(request.cwd)

    val renderer: Seq[Segment] => String = request.shellName.toLowerCase match {
      case "bash" => shells.BashPrompt.render
      case "zsh" => shells.ZshPrompt.render
      case x => (seg) => s"$x is an unknown shell"
    }

    val prompt = generator.generate(request)
    val promptText = renderer(prompt)

    log.debug(s"Prompt: $promptText")

    Future.successful(ByteString(promptText))
  }

  def parsePromptRequest(data: Seq[String]): PromptRequest = {
    val in = data.toIndexedSeq
    val shell = in(0)
    val currentDirectory = in(1)
    val previousStatusTxt = in(2)
    val previousStatus = previousStatusTxt.toInt
    val winWidthTxt = in(3)
    val winWidth = winWidthTxt.toInt
    val home = in(4)
    val user = in(5)

    PromptRequest(shell, new File(currentDirectory), previousStatus, winWidth, new File(home), user)
  }

  def parseDirSearchRequest(data: Seq[String]): DirHistorySearchReq = {
    DirHistorySearchReq(data(0))
  }

}