package powerline.shells

import java.io.File

import powerline._
import powerline.vcs.VCSRepo

object PromptGenerator {
  val homeSymbol = "~"
  val filledSeparator = "\uE0B0"
  val thinSeparator = "\uE0B1"
  val ellipsis = "\u2026"

  val dirSeparator = thinSeparator
  val vcsSymbol = "\uE0A0"
}

final class PromptGenerator(config: AppConfig) {

  import PromptGenerator._

  private def theme = config.theme

  def generate(request: Request): Seq[Segment] = {

    // calculate max length for PWD segments
    val maxPromptLen = ((request.winWidth + request.home.length - 3) * 0.4f).toInt

    val separator = Seq(SectionSeparator(filledSeparator))

    userSegments(request.username) ++
      separator ++
      pathSegments(request.cwd, maxPromptLen, request.home) ++
      separator ++
      request.vcs.map(vcsStatus).getOrElse(Seq()) ++
      previousCmdIndicator(request.previousCmdStatus)
  }

  private[shells] def userSegments(user: String): Seq[Segment] = Seq(
    TextSegment(s" $user ", theme.user)
  )
  
  
  private[shells] def pathSegments(path: File, maxLen: Int, home: File): Seq[Segment] = {
    val pathWithHome = path.getAbsolutePath.replace(home.getAbsolutePath, homeSymbol)
    val parts = pathWithHome.split("/")

    if (parts.size == 1) {
      Seq(TextSegment(s" ${parts.head} ", theme.cwd))
    } else {
      parts.init.flatMap(part =>
        Seq(TextSegment(s" $part ", theme.path), TextSegment(dirSeparator, theme.path))
      ) :+ TextSegment(s" ${parts.last} ", theme.cwd)
    }
  }

  private[shells] def vcsStatus(repo: VCSRepo): Seq[Segment] =
    Seq(TextSegment(
      s" $vcsSymbol ${repo.currentBranch} ",
      if (repo.isClean) theme.repoClean
      else theme.repoDirty
    ))

  private[shells] def previousCmdIndicator(retCode: Int): Seq[Segment] =
    Seq(TextSegment(
      s" $filledSeparator",
      if (retCode != 0) theme.cmdFailed
      else theme.cmdPassed)
    )
}