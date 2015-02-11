package powerline.shells

import powerline._

object ZshPrompt {

  import BashPrompt._

  def renderSection(section: Section) =
    section.segments.map(segment => styled(segment.text, segment.style))

  def render(prompt: Seq[Segment]): String =
    prompt.map(seg => styled(seg.text, seg.style)).mkString + reset


}
