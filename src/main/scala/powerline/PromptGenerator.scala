package powerline

import java.io.File

import powerline.vcs.RepoStatus

object PromptGenerator {

  val filledSeparator = "\uE0B0"
  val thinSeparator = "\uE0B1"
  val ellipsis = "\u2026"

  val dirSeparator = thinSeparator
  val vcsSymbol = "\uE0A0"
  val lock = "\uD83D\uDD12"

  def generate(theme: Theme, request: PromptRequest, repoStatus: Option[RepoStatus]): Seq[Segment] = {

    // create the basic segments
    val sections: Seq[Section] = Seq(
      Some(userSegments(request.username, theme)),
      Some(pathSegments(request.cwd, request.home, request.maxPromptLength, theme)),
      repoStatus.map(status => vcsStatus(status, theme))
    ).flatten

    val promptWithSeparators = interleaveSeparators(sections)

    // handle prompt end differently, no separator between that and previous
    promptWithSeparators ++ Seq(Segment(filledSeparator,
      if (request.previousCmdStatus == 0) theme.cmdPassed
      else theme.cmdFailed
    ), Segment(" ", theme.cmdPassed))
  }

  private[powerline] def interleaveSeparators(sections: Seq[Section]): Seq[Segment] = {
    // add separators
    val options = sections.map(Some.apply)
    val shifted = options.tail :+ None

    val segmentAndNext = options.zip(shifted)

    segmentAndNext.flatMap {

      case (Some(section), Some(next)) =>
        val sepFg = section.segments.last.style.bg
        val separator = Segment(PromptGenerator.filledSeparator, Style(sepFg, next.segments.head.style.bg))

        section.segments :+ separator


      case (Some(section), None) =>
        // last element, don't add any separator
        section.segments

    }

  }


  private[powerline] def userSegments(user: String, theme: Theme) =
    Section(Seq(Segment(s" $user ", theme.user)))


  private[powerline] def pathSegments(path: File, home: File, maxPromptLength: Option[Int], theme: Theme): Section = {
    val promptifiedPath = Path.promptify(path, home, maxPromptLength)

    var cwd: String =
      if (promptifiedPath.length == 1) promptifiedPath.head
      else promptifiedPath.last

    val parents: Seq[String] =
      if (promptifiedPath.length == 1) Seq()
      else promptifiedPath.init

    cwd =
      if (!path.canWrite) lock + " " + cwd
      else cwd

    // separate with symbol and pad names
    val segments = parents.flatMap(part =>
      Seq(Segment(s" $part ", theme.path), Segment(dirSeparator, theme.separator))
    ) :+ Segment(s" $cwd ", theme.cwd)

    Section(segments)
  }


  private[powerline] def vcsStatus(status: RepoStatus, theme: Theme) = {
    println(status)
    val bStatus = branchStatus(status)
    Section(Seq(Segment(
      s" $vcsSymbol ${status.label}$bStatus",
      if (status.dirty) theme.repoDirty
      else if (status.detached) theme.repoDetached
      else theme.repoClean
    )))
  }

  private[powerline] def branchStatus(status: RepoStatus): String = {
    val ahead = status.ahead
    val behind = status.behind

    if (ahead > 0 || behind > 0) {
      val aText = if (ahead > 0) s"↑$ahead" else ""
      val bText = if (behind > 0) s"↓$behind" else ""

      s" $aText $bText"
    } else {
      " "
    }
  }

  private[powerline] def previousCmdIndicator(retCode: Int, theme: Theme): Seq[Segment] =
    Seq(Segment(
      s" $filledSeparator",
      if (retCode != 0) theme.cmdFailed
      else theme.cmdPassed)
    )
}