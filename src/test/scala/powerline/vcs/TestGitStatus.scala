package powerline.vcs

import java.io.File

import org.scalatest.FunSuite

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
