package powerline.vcs

import java.io.File

import akka.actor.{ActorLogging, Props, Actor}
import powerline.FileSystem

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.util.{Failure, Success}


object Repositories {

  // protocol
  case class GetRepoStatus(path: File)
  case class Result(status: Option[RepoStatus])

  def props = Props(new Repositories)

}


/**
 * Maintains a cache of known, recently accessed repositories to quicken up access
 */
class Repositories extends Actor with ActorLogging {

  import Repositories._

  def receive = {

    case GetRepoStatus(path) =>
      GitRepo.status(path) match {
        case Success(status) => sender() ! Result(Some(status))
        case Failure(ex) =>
          //log.error(ex, "Failed to read git status of {}", path)
          sender() ! Result(None)
      }

  }

}
