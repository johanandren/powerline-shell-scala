package powerline.vcs

import java.io.File

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.BranchTrackingStatus


case class GitRepo(path: File) extends Repository {

  private val git = Git.open(path)

  def status = {
    val gitStatus = git.status().call()
    val branchName = git.getRepository.getBranch
    val branchStatus = Option(BranchTrackingStatus.of(git.getRepository, branchName))

    RepoStatus(
      dirty = !gitStatus.isClean,
      behind = branchStatus.fold(0)(_.getBehindCount),
      ahead = branchStatus.fold(0)(_.getAheadCount),
      label = branchName
    )
  }

  def close(): Unit = {
    git.close()
  }

}