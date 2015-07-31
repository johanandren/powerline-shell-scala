package powerline.vcs

import java.io.File

import akka.actor.{ActorLogging, Props, Actor}
import powerline.FileSystem

import scala.concurrent.duration._


object Repositories {

  // protocol
  case class GetRepoStatus(path: File)
  case class Result(status: Option[RepoStatus])

  def props = Props(new Repositories(30 minutes))

}


/**
 * Maintains a cache of known, recently accessed repositories to quicken up access
 */
class Repositories(maxRepoAge: FiniteDuration) extends Actor with ActorLogging {

  import Repositories._

  case object CleanOut
  import context.dispatcher

  val tick = context.system.scheduler.schedule(maxRepoAge / 2, maxRepoAge / 2, self, CleanOut)

  case class Entry(path: File, lastTouched: Long, repo: Repository)

  var repositories = Seq.empty[Entry]

  def receive = {

    case GetRepoStatus(path) =>
      val maybeRepo: Option[Repository] = knownRepo(path) orElse {
        val maybeNewRepo = findRepo(path)
        maybeNewRepo.foreach(repo => repositories :+ Entry(path, System.currentTimeMillis(), repo))
        maybeNewRepo
      }
      sender() ! Result(maybeRepo.map(_.status))
      maybeRepo.foreach(touchRepo)

    case CleanOut =>
      val timeout = System.currentTimeMillis() - maxRepoAge.toMillis
      val (tooOld, keepThese) = repositories.partition(_.lastTouched < timeout)
      repositories = keepThese
      tooOld.foreach(_.repo.close())

  }

  def touchRepo(repo: Repository): Unit = {
    val index = repositories.indexWhere(_.repo == repo)
    if (index > 0) {
      val entry = repositories(index)
      repositories = repositories.updated(index, entry.copy(lastTouched = System.currentTimeMillis()))
    }
  }

  def knownRepo(file: File): Option[Repository] =
    repositories.collectFirst {
      case Entry(`file`, _, repo) => repo
      case Entry(path, _, repo) if FileSystem.isInside(file, path) => repo
    }

  def findRepo(path: File): Some[Repository] = {
    if (path == null) None
    val git = new File(path, ".git")
    if (git.exists()) Some(GitRepo(git))
    else findRepo(path.getParentFile)
  }


  override def postStop(): Unit = {
    repositories.foreach(_.repo.close())
    tick.cancel()
  }
}
