package powerline

import java.io.File

import powerline.vcs.GitRepo


case class AppConfig(
    theme: Theme,
    debug: Boolean = false,
    serverPort: Int = 18888)

sealed trait Request

case object LastDirRequest extends Request
case class DirHistorySearchReq(query: String) extends Request
case class PromptRequest(
    shellName: String,
    cwd: File,
    previousCmdStatus: Int,
    winWidth: Int,
    home: File,
    username: String) extends Request {

  val maxPromptLengthPercent = 0.3f
  val maxPromptLength = (winWidth * maxPromptLengthPercent).toInt
}



case class Segment(text: String, style: Style)
case class Section(segments: Seq[Segment])
