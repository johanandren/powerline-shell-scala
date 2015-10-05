package powerline

import java.io.File

import powerline.vcs.GitRepo


case class AppConfig(
    debug: Boolean = false,
    serverPort: Int = 18888)

sealed trait Request

case object LastDirRequest extends Request
case class DirHistorySearchReq(query: String) extends Request
case class PromptRequest(
    theme: String,
    shellName: String,
    cwd: File,
    previousCmdStatus: Int,
    winWidth: Option[Int],
    home: File,
    username: String) extends Request {

  val maxPromptLengthPercent = 0.3f
  val maxPromptLength = winWidth.map(cols => (cols * maxPromptLengthPercent).toInt)
}



case class Segment(text: String, style: Style)
case class Section(segments: Seq[Segment])
