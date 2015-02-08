package powerline.vcs

import java.io.File

import org.eclipse.jgit.api.Git

object GitRepo {

  def apply(path: File): Option[GitRepo] =
    try {
      if (path.getAbsolutePath == "/") None  // TODO bug in JGit ???
      else Some(new GitRepo(Git.open(path)))
    } catch {
      case e: Exception => None
    }
  
}

/**
 * Checks git repo status (clean, branch)
 */
class GitRepo(git: Git) extends VCSRepo {

  def isClean =
    git.status().call().isClean

  def currentBranch =
    git.getRepository.getBranch
}