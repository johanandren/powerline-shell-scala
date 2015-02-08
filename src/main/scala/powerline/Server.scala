package powerline

import java.io.{File, BufferedReader, InputStreamReader, PrintStream}
import java.net.{InetAddress, ServerSocket, Socket}

import powerline.vcs.GitRepo

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class Server(config: AppConfig) {

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

      val req = createRequest(in)
      debug(s"Request: $req")

      req.map { request =>

        val promptText = config.shell.newPromptText(request)

        debug(s"Prompt: $promptText")
        out.print(promptText)
      }

      in.close()
      out.close()
      socket.close()

    } recover {
      case x: Exception =>
        debug(s"Got error handling connection $x")
        throw x
    }
  }


  private def createRequest(in: BufferedReader): Try[Request] =
    parseProtocol(in).map { case (cwd, previousStatus, winWidth) =>
      val vcsRepo = GitRepo(cwd)

      // TODO should not come from here but from the message, (the user may have sudo:d etc.)
      val home = new File(System.getenv("HOME"))
      val username = System.getenv("USER")

      Request(cwd, previousStatus, winWidth, home, username, vcsRepo)
    }


  private def parseProtocol(in: BufferedReader): Try[(File, Int, Int)] =
    for {
      cwd <- Try(in.readLine())
      previousStatusTxt <- Try(in.readLine())
      winWidthTxt <- Try(in.readLine())
      previousStatus <- Try(previousStatusTxt.toInt)
      winWidth <- Try(winWidthTxt.toInt)
    } yield (new File(cwd), previousStatus, winWidth)


 private def debug(msg: => String): Unit = if (config.debug) println(msg)

}