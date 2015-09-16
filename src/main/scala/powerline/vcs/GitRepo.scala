package powerline.vcs

import java.io.File

import scala.sys.process._

case class GitRepo(path: File) extends Repository {

  def status = {
    val revParseOutput = Process(Seq("git", "rev-parse", "--abbrev-ref", "HEAD"), Some(path)).!!.trim

    val branchName =
      if (revParseOutput != "HEAD") revParseOutput
      // detached, use short checksum
      else Process(Seq("git", "rev-parse", "--short", "HEAD")).!!.trim

    val dirty = Process(Seq("git", "status", "--porcelain"), Some(path)).lineStream.toSeq.nonEmpty
    val behind = Process(Seq("git", "rev-list", "HEAD..origin", "--count"), Some(path)).!!.trim.toInt
    val ahead = Process(Seq("git", "rev-list", "origin..HEAD", "--count"), Some(path)).!!.trim.toInt

    RepoStatus(dirty, behind, ahead, branchName)
  }

  def close(): Unit = {
  }

}