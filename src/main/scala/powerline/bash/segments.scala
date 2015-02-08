package powerline.bash

import powerline.Segment

sealed trait BashSegment extends Segment {
  def contentFg: Int
  def contentBg: Int
}

case class NormalSegment(content: String,
                         contentFg: Int,
                         contentBg: Int,
                         sep: String,
                         sepFg: Int)
  extends BashSegment {

  import powerline.bash.BashPrompt._

  override def draw(next: Option[Segment]): String = {
    List(fgcolor(contentFg), bgcolor(contentBg), content,
      fgcolor(sepFg), bgcolor(contentBg), sep) mkString ""
  }
}

case class LastSegment(content: String, contentFg: Int, contentBg: Int) extends BashSegment {

  import powerline.bash.BashPrompt._

  override def draw(next: Option[Segment]): String = {
    val sepBgColorStr = next match {
      case Some(seg: BashSegment) => bgcolor(seg.contentBg)
      case None => RESET
    }
    List(fgcolor(contentFg), bgcolor(contentBg), content,
      sepBgColorStr, fgcolor(contentBg), filledSeparator) mkString ""
  }
}
