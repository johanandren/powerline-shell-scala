package powerline

import java.io.File

import powerline.vcs.{GitRepo, VCSRepo}


case class AppConfig(
    theme: Theme,
    debug: Boolean = false,
    serverPort: Int = 18888)

case class Request(
    shellName: String,
    cwd: File,
    previousCmdStatus: Int,
    winWidth: Int,
    home: File,
    username: String) extends {

  lazy val vcs: Option[VCSRepo] = GitRepo(cwd)
  val maxPromptLengthPercent = 0.3f
  val maxPromptLength = (winWidth * maxPromptLengthPercent).toInt
}



case class Segment(text: String, style: Style)
case class Section(segments: Seq[Segment])
