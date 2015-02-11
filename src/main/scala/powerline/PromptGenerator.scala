package powerline

import java.io.File

import powerline.vcs.{GitRepo, VCSRepo}

object PromptGenerator {
  val homeSymbol = "~"
  val filledSeparator = "\uE0B0"
  val thinSeparator = "\uE0B1"
  val ellipsis = "\u2026"

  val dirSeparator = thinSeparator
  val vcsSymbol = "\uE0A0"
}

final class PromptGenerator(config: AppConfig) {

  import powerline.PromptGenerator._

  private def theme = config.theme

  def generate(request: Request): Seq[Segment] = {

    // calculate max length for PWD segments
    val maxPromptLen = ((request.winWidth + request.home.length - 3) * 0.4f).toInt

    // create the basic segments
    val sections: Seq[Section] = Seq(
      Some(userSegments(request.username)),
      Some(pathSegments(request.cwd, maxPromptLen, request.home)),
      request.vcs.map(vcsStatus)
    ).flatten

    val promptWithSeparators = interleaveSeparators(sections)

    promptWithSeparators :+ Segment(thinSeparator,
      if (request.previousCmdStatus == 0) theme.cmdPassed
      else theme.cmdFailed
    )
  }

  private[powerline] def interleaveSeparators(sections: Seq[Section]): Seq[Segment] = {
    // add separators
    val options = sections.map(Some.apply)
    val shifted = options.tail :+ None

    val segmentAndNext = options.zip(shifted)

    segmentAndNext.map {

      case (Some(section), Some(next)) =>
        val sepFg = section.segments.last.style.bg
        val separator = Segment(PromptGenerator.filledSeparator, Style(sepFg, next.segments.head.style.bg))

        section.segments :+ separator


      case (Some(section), None) =>
        // last element, don't add any separator
        section.segments

    }.flatten

  }


  private[powerline] def userSegments(user: String) =
    Section(Seq(Segment(s" $user ", theme.user)))


  private[powerline] def pathSegments(path: File, maxLen: Int, home: File) = {
    val pathWithHome = path.getAbsolutePath.replace(home.getAbsolutePath, homeSymbol)
    val parts = pathWithHome.split("/")

    val segments: Seq[Segment] =
      if (parts.size == 1) {
        Seq(Segment(s" ${parts.head} ", theme.cwd))
      } else {
        parts.init.flatMap(part =>
          Seq(Segment(s" $part ", theme.path), Segment(dirSeparator, theme.separator))
        ) :+ Segment(s" ${parts.last} ", theme.cwd)
      }
    Section(segments)
  }

  private[powerline] def vcsStatus(repo: VCSRepo) = {
    val modifier =
      if (repo.isClean) ""
      else "*"

    val extra = repo match {
      case g: GitRepo => gitExtra(g)
      case _ => ""
    }

    Section(Seq(Segment(
      s" $vcsSymbol ${repo.currentBranch}$modifier $extra",
      if (repo.isClean) theme.repoClean
      else theme.repoDirty
    )))
  }

  private[powerline] def gitExtra(repo: GitRepo): String = {
    val ahead = repo.ahead

    val aText =
      if (ahead > 0) s"↑$ahead"
      else ""

    val behind = repo.behind
    val bText =
      if (behind > 0) s"↓$behind"
      else ""

    s"$aText $bText"
  }

  private[powerline] def previousCmdIndicator(retCode: Int): Seq[Segment] =
    Seq(Segment(
      s" $filledSeparator",
      if (retCode != 0) theme.cmdFailed
      else theme.cmdPassed)
    )
}