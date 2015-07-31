package powerline.vcs

import java.io.File

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.BranchTrackingStatus

object GitRepo {

  // recursively looks for a git repo in the given path
  def apply(path: File): Option[GitRepo] =
    try {
      // if we got to root, bail
      if (path.getAbsolutePath == "/") None
      else Some(new GitRepo(Git.open(path)))
    } catch {
      case e: Exception =>
        apply(path.getParentFile)
    }
}

class GitRepo(git: Git) extends VCSRepo {

  lazy val status = git.status().call()
  def clean = status.isClean

  lazy val trackingStatus = Option(BranchTrackingStatus.of(git.getRepository, git.getRepository.getBranch))

  def behind: Option[Int] = trackingStatus.map(_.getBehindCount)
  def ahead: Option[Int] = trackingStatus.map(_.getAheadCount)

  def currentBranch =
    git.getRepository.getBranch

  def close(): Unit = {
    git.close()
  }


}