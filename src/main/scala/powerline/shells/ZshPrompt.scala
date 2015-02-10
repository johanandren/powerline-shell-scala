package powerline.shells

import powerline._

object ZshPrompt {

  def fgColor(color: Color): String = s"\033[38;5;${color.n}m"

  def bgColor(color: Color): String = s"\033[48;5;${color.n}m"

  val reset = "\033[0m"

  def styled(str: String, style: Style): String =
    fgColor(style.fg) + bgColor(style.bg) + str + reset

  def render(prompt: Seq[Segment]): String =
    prompt.map {
      case TextSegment(text, style) => styled(text, style)
      case SectionSeparator(text) => text
    }.mkString("")

}