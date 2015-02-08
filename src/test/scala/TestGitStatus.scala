import java.io.File

import org.scalatest.FunSuite
import powerline.vcs.GitRepo

/**
 */
class TestGitStatus extends FunSuite {
  test("git status called in non-repo folder") {
    assert(GitRepo(new File("/")).isEmpty)
  }

  test("git status called in repo folder") {
    assert(GitRepo(new File(".")).nonEmpty)
  }
}
