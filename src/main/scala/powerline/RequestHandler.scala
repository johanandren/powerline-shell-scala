package powerline

import java.io.File

import akka.actor.{Props, ActorLogging, Actor, ActorRef}
import akka.io.Tcp.{PeerClosed, Write, Received}
import akka.pattern.{AskTimeoutException, ask, pipe}
import akka.util.{Timeout, ByteString}
import powerline.vcs.Repositories

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Failure

/**
 * The request handler handles one request and then shuts down
 */
object RequestHandler {

  def props(config: AppConfig, directoryHistory: ActorRef, repositories: ActorRef) =
    Props(new RequestHandler(config, directoryHistory, repositories))

  def parseRequest(data: ByteString): Request = {
    data.decodeString("UTF-8").split('\n').toSeq match {
      case "prompt" +: rest => parsePromptRequest(rest)
      case "dirsearch" +: rest => parseDirSearchRequest(rest)
      case Seq("lastdir") => LastDirRequest
      case wat +: _ => throw new RuntimeException(s"Unknown request type $wat")
      case _ => throw new RuntimeException("Malformed request")
    }
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

class RequestHandler(config: AppConfig, directoryHistory: ActorRef, repositories: ActorRef) extends Actor with ActorLogging {

  import RequestHandler._
  import context.dispatcher

  case class Response(recipient: ActorRef, response: ByteString)

  def receive = {
    case Received(data) =>
      val recipient = sender()
      (parseRequest(data) match {
        case prompt: PromptRequest => handlePromptRequest(prompt)
        case dir: DirHistorySearchReq => handleDirSearchReq(dir)
        case LastDirRequest => handleLastDirRequest()
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


  val generator = new PromptGenerator(config)


  def handleLastDirRequest(): Future[ByteString] = {
    implicit val timeout = Timeout(2.seconds)
    (directoryHistory ? DirectoryHistory.GetLastDirectory)
      .mapTo[DirectoryHistory.LastDirectory]
      .map { result =>
      ByteString(result.directory.fold("")(_.getAbsolutePath))
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

    val repoStatusF = {
      implicit val timeout = Timeout(2 seconds)
      (repositories ? Repositories.GetRepoStatus(request.cwd))
        .mapTo[Repositories.Result]
        .map(_.status)
        .recover {
        case _: AskTimeoutException => None
      }
    }

    directoryHistory ! DirectoryHistory.DirectoryVisited(request.cwd)


    val renderer: Seq[Segment] => String = request.shellName.toLowerCase match {
      case "bash" => shells.BashPrompt.render
      case "zsh" => shells.ZshPrompt.render
      case x => (seg) => s"$x is an unknown shell"
    }

    for {
      repoStatus <- repoStatusF
    } yield {
      val prompt = generator.generate(request, repoStatus)
      val promptText = renderer(prompt)

      log.debug(s"Prompt: $promptText")
      ByteString(promptText)
    }

  }

}