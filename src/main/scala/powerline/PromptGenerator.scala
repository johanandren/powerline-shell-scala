package powerline

import java.io.File

import powerline.vcs.{GitRepo, VCSRepo}

object PromptGenerator {

  val filledSeparator = "\uE0B0"
  val thinSeparator = "\uE0B1"
  val ellipsis = "\u2026"

  val dirSeparator = thinSeparator
  val vcsSymbol = "\uE0A0"
  val lock = "\uD83D\uDD12"
}

final class PromptGenerator(config: AppConfig) {

  import powerline.PromptGenerator._

  private def theme = config.theme

  def generate(request: Request): Seq[Segment] = {

    // calculate max length for PWD segments

    // create the basic segments
    val sections: Seq[Section] = Seq(
      Some(userSegments(request.username)),
      Some(pathSegments(request.cwd, request.home, request.maxPromptLength)),
      request.vcs.map(vcsStatus)
    ).flatten

    val promptWithSeparators = interleaveSeparators(sections)

    promptWithSeparators :+ Segment(filledSeparator,
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


  private[powerline] def pathSegments(path: File, home: File, maxPromptLength: Int): Section = {
    val promptifiedPath = Path.promptify(path, home, maxPromptLength)

    println(maxPromptLength)
    var cwd: String =
      if (promptifiedPath.length == 1) promptifiedPath(0)
      else promptifiedPath.last

    val parents: Seq[String] =
      if (promptifiedPath.length == 1) Seq()
      else promptifiedPath.init

    cwd =
      if (!path.canWrite) lock + " " + cwd
      else cwd

    val segments = parents.flatMap(part =>
      Seq(Segment(s" $part ", theme.path), Segment(dirSeparator, theme.separator))
    ) :+ Segment(s" $cwd ", theme.cwd)

    Section(segments)
  }


  private[powerline] def vcsStatus(repo: VCSRepo) = {
    val extra = repo match {
      case g: GitRepo => gitExtra(g)
      case _ => ""
    }

    Section(Seq(Segment(
      s" $vcsSymbol ${repo.currentBranch} $extra",
      if (repo.clean) theme.repoClean else theme.repoDirty
    )))
  }

  private[powerline] def gitExtra(repo: GitRepo): String = {
    val ahead = repo.ahead

    val aText = ahead.filter(_ > 0).map(a => s"↑$a").getOrElse("")

    val behind = repo.behind
    val bText = behind.filter(_ > 0).map(b => s"↓$b").getOrElse("")

    s"$aText $bText"
  }

  private[powerline] def previousCmdIndicator(retCode: Int): Seq[Segment] =
    Seq(Segment(
      s" $filledSeparator",
      if (retCode != 0) theme.cmdFailed
      else theme.cmdPassed)
    )
}