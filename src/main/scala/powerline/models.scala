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

}



sealed trait Segment

case class TextSegment(content: String, style: Style) extends Segment
case class SectionSeparator(separator: String) extends Segment