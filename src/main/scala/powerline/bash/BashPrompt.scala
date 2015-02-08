package powerline.bash

import java.io.File

import powerline.vcs.{VCSRepo, GitRepo}
import powerline.{PathUtils, Request, Segment, PromptGenerator}

object BashPrompt extends PromptGenerator {

  /** ANSI color definitions */
  object Color {
    val PATH_BG = 238  // dark grey
    val PATH_FG = 251  // light grey
    val CWD_FG = 254  // nearly-white grey
    val SEPARATOR_FG = 244

    val USER_FG = 27

    val REPO_CLEAN_BG = 35 // 148  // a light green color
    val REPO_CLEAN_FG = 0  // black
    val REPO_DIRTY_BG = 160 // 161  // pink/red
    val REPO_DIRTY_FG = 15  // white

    val CMD_PASSED_BG = 236
    val CMD_PASSED_FG = 15
    val CMD_FAILED_BG = 124 // 161
    val CMD_FAILED_FG = 15

    val SVN_CHANGES_BG = 148
    val SVN_CHANGES_FG = 22  // dark green

    val VIRTUAL_ENV_BG = 35  // a mid-tone green
    val VIRTUAL_ENV_FG = 22
  }

  // Separators
  val filledSeparator = "\u2B80"
  val thinSeparator = "\u2B81"
  val ellipsis = "\u2026"

  // Bash prompt color escape string
  val LSQESCRSQ = "\\[\\e%s\\]"
  val RESET = LSQESCRSQ format "[0m"
  val ROOT_INDICATOR = " \\$ "

  def color(prefix: String, code: Int) =
    LSQESCRSQ format ("[%s;5;%sm" format (prefix, code))

  def fgcolor(code: Int) =
    color("38", code)

  def bgcolor(code: Int) =
    color("48", code)


  override def newPrompt(request: Request): Seq[Segment] = {
    // calculate max length for PWD segments
    val maxPromptLen = ((request.winWidth + request.home.length - 3) * 0.4f).toInt

    userSegment(request.username) ++
      generateCwdSegments(request.cwd, maxPromptLen, request.home) ++
      request.vcs.map(generateVCSSegment).getOrElse(Seq()) ++
      generateRootIndicator(request.previousCmdStatus)

  }


  private def generateCwdSegments(path: File, maxLen: Int, home: File) = {
    val (truncated, pathParts) = PathUtils.cwdString(path, home, maxLen)
    val segments = pathParts.init.map(pathSegment)

    val start =
      if (truncated) IndexedSeq(pathSegment(s" $ellipsis "))
      else IndexedSeq()

    start ++ segments :+ LastSegment(s" ${pathParts.last} ", Color.PATH_FG, Color.PATH_BG)
  }

  private def pathSegment(name: String) =
    NormalSegment(s" $name ", Color.PATH_FG, Color.PATH_BG, thinSeparator, Color.SEPARATOR_FG)


  private def generateRootIndicator(retCode: Int) = {
    val (fg, bg) =
      if (retCode != 0) (Color.CMD_FAILED_FG, Color.CMD_FAILED_BG)
      else (Color.CMD_PASSED_FG, Color.CMD_PASSED_BG)

    IndexedSeq(LastSegment(ROOT_INDICATOR, fg, bg))
  }

  private def generateVCSSegment(repo: VCSRepo): Seq[Segment] = {
    val (fg, bg) =
      if (repo.isClean) (Color.REPO_CLEAN_FG, Color.REPO_CLEAN_BG)
      else (Color.REPO_DIRTY_FG, Color.REPO_DIRTY_BG)

    IndexedSeq(LastSegment(s" ${repo.currentBranch} ", fg, bg))
  }


  private def userSegment(user: String) = IndexedSeq(
    LastSegment(" %s " format user, Color.REPO_DIRTY_FG, Color.USER_FG))

}


