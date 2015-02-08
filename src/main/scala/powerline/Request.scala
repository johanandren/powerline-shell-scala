package powerline

import java.io.File

import powerline.vcs.VCSRepo

case class Request(
    cwd: File,
    previousCmdStatus: Int,
    winWidth: Int,
    home: File,
    username: String,
    vcs: Option[VCSRepo])