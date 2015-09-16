package powerline.vcs

import java.io.File

import scala.sys.process._

case class GitRepo(path: File) extends Repository {

  def status = {
    val revParseOutput = Process(Seq("git", "rev-parse", "--abbrev-ref", "HEAD"), Some(path)).!!.trim

    if (revParseOutput != "HEAD") {
      val label = revParseOutput
      val remote = s"origin/$label"
      val dirty = Process(Seq("git", "status", "--porcelain"), Some(path)).lineStream.toSeq.nonEmpty
      val behind = Process(Seq("git", "rev-list", s"HEAD..$remote", "--count"), Some(path)).!!.trim.toInt
      val ahead = Process(Seq("git", "rev-list", s"$remote..HEAD", "--count"), Some(path)).!!.trim.toInt

      RepoStatus(dirty, behind, ahead, label)
    } else {

      // detached, use short checksum
      val label = Process(Seq("git", "rev-parse", "--short", "HEAD")).!!.trim
      RepoStatus(false, 0, 0, label)
    }

  }

  def close(): Unit = {
  }

}