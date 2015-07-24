package powerline

import java.io._
import java.net.{InetAddress, ServerSocket, Socket}
import java.util.concurrent.Executors

import akka.io.Tcp.{Write, Received, PeerClosed}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import akka.actor.{ActorLogging, Actor, ActorRef, Props}
import akka.io.{ IO, Tcp }
import akka.util.ByteString
import java.net.InetSocketAddress

class Server(config: AppConfig) extends Actor with ActorLogging {

  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 18888))

  def receive = {
    case b@Bound(localAddress) =>
      log.info(s"Server started at $localAddress")

    case CommandFailed(_: Bind) =>
      log.error("Failed to bind server")
      context.system.shutdown()

    case c@Connected(remote, local) =>
      val handler = context.actorOf(Props(classOf[RequestHandler], config))
      val connection = sender()
      connection ! Register(handler)
  }
}

class RequestHandler(config: AppConfig) extends Actor with ActorLogging {

  def receive = {
    case Received(data) =>
      parseRequest(data)
        .map(handleRequest)
        .foreach(response =>
          sender() ! Write(response)
        )
      context stop self

    case PeerClosed     =>
      context stop self
  }


  // implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))

  val generator = new PromptGenerator(config)

  def handleRequest(request: Request): ByteString = {
    log.debug(s"Request: $request")

    val renderer: Seq[Segment] => String = request.shellName.toLowerCase match {
      case "bash" => shells.BashPrompt.render
      case "zsh" => shells.ZshPrompt.render
      case x => (seg) => s"$x is an unknown shell"
    }

    val prompt = generator.generate(request)
    val promptText = renderer(prompt)

    log.debug(s"Prompt: $promptText")

    ByteString(promptText)
  }

  private def parseRequest(data: ByteString): Try[Request] =
    for {
      in <- Try(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data.toArray))))
      shell <- Try(in.readLine())
      currentDirectory <- Try(in.readLine())
      previousStatusTxt <- Try(in.readLine())
      previousStatus <- Try(previousStatusTxt.toInt)
      winWidthTxt <- Try(in.readLine())
      winWidth <- Try(winWidthTxt.toInt)
      home <- Try(in.readLine())
      user <- Try(in.readLine())
    } yield Request(shell, new File(currentDirectory), previousStatus, winWidth, new File(home), user)

}