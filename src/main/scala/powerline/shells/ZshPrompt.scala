package powerline.shells

import powerline._

object ZshPrompt {

  def styled(text: String, style: Style): String =
    s"%F{${style.fg.n}}%K{${style.bg.n}}$text%k%f"

  def renderSection(section: Section) =
    section.segments.map(segment => styled(segment.text, segment.style))

  def render(prompt: Seq[Segment]): String =
    prompt.map(seg => styled(seg.text, seg.style)).mkString

}
