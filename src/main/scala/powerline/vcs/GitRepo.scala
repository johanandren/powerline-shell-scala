package powerline.vcs

import java.io.File

import scala.sys.process._
import scala.util.Try
import scala.util.parsing.combinator.{RegexParsers, JavaTokenParsers}

object GitRepo {

  def status(dir: File): Try[RepoStatus] = {
    Try {
      def sh(cmd: String, args: String*): ProcessBuilder =
        Process(cmd +: args.toSeq, dir)

      val revParseOutput = sh("git", "rev-parse", "--abbrev-ref", "HEAD").!!.trim

      if (revParseOutput != "HEAD") {
        val label = revParseOutput
        val remote = s"origin/$label"
        val dirty = sh("git", "status", "--porcelain").lineStream.toSeq.nonEmpty

        val (behind, ahead, hasRemote) = try {
          val behind = sh("git", "rev-list", s"HEAD..$remote", "--count").!!.trim.toInt
          val ahead = sh("git", "rev-list", s"$remote..HEAD", "--count").!!.trim.toInt
          (behind, ahead, true)
        } catch {
          case _: Exception => (0, 0, false)
        }

        RepoStatus(dirty, behind, ahead, label, hasRemote = hasRemote, detached = false)
      } else {

        // detached, use short checksum
        val label = sh("git", "rev-parse", "--short", "HEAD").!!.trim
        RepoStatus(
          dirty = false,
          behind = 0,
          ahead = 0,
          label = label,
          hasRemote = false,
          detached = true)
      }
    }
  }

}