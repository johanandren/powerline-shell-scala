package powerline.shells

import powerline._

object ZshPrompt {

  def styled(text: String, style: Style, builder: StringBuilder): Unit = {
    builder.append("%F{")
      .append(style.fg.n.toString)
      .append("}%K{")
      .append(style.bg.n.toString)
      .append("}")
      .append(text)
      .append("%k%f")
  }

  def render(prompt: Seq[Segment]): String = {
    val builder = new StringBuilder(80)
    prompt.foreach { seg =>
      styled(seg.text, seg.style, builder)
    }
    builder.toString()
  }

}
