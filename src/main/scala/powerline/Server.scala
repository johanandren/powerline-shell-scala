package powerline

import java.io.{File, BufferedReader, InputStreamReader, PrintStream}
import java.net.{InetAddress, ServerSocket, Socket}

import powerline.shells.PromptGenerator
import powerline.vcs.GitRepo

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class Server(config: AppConfig) {

  val generator = new PromptGenerator(config)

  def start() {
    try {
      val server = new ServerSocket(config.serverPort, 0, InetAddress.getByName(null))
      while (true) {
        val socket = server.accept()
        handleConnection(socket)
      }
    } catch {
      case e: Exception =>
        println("Failed to start server")
        e.printStackTrace()
    }
  }


  def handleConnection(socket: Socket): Unit = {
    Future {
      val in = new BufferedReader(new InputStreamReader(socket.getInputStream))
      val out = new PrintStream(socket.getOutputStream)

      val req = parseRequest(in)
      debug(s"Request: $req")

      val request = req.get

      val renderer = request.shellName.toLowerCase match {
        case "bash" => shells.BashPrompt.render _
        case "zsh" => shells.ZshPrompt.render _
        case x => (seg: Seq[Segment]) => s"$x is an unknown shell"
      }

      val prompt = generator.generate(request)
      val promptText = renderer(prompt)

      debug(s"Prompt: $promptText")
      out.print(promptText)


      in.close()
      out.close()
      socket.close()

    } recover {
      case x: Exception =>
        debug(s"Got error handling connection $x")
        x.printStackTrace()
        throw x
    }
  }

  private def parseRequest(in: BufferedReader): Try[Request] =
    for {
      shell <- Try(in.readLine())
      currentDirectory <- Try(in.readLine())
      previousStatusTxt <- Try(in.readLine())
      previousStatus <- Try(previousStatusTxt.toInt)
      winWidthTxt <- Try(in.readLine())
      winWidth <- Try(winWidthTxt.toInt)
      home <- Try(in.readLine())
      user <- Try(in.readLine())
    } yield Request(shell, new File(currentDirectory), previousStatus, winWidth, new File(home), user)


 private def debug(msg: => String): Unit = if (config.debug) println(msg)

}