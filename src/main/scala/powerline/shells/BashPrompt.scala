package powerline.shells

import powerline._

object BashPrompt {

  // Bash prompt color escape string
  private def asciiEscape(str: String) = s"""\\[\\e$str\\]"""
  private val reset = asciiEscape("[0m")

  def color(prefix: String, code: Int) =
    asciiEscape(s"[$prefix;5;${code}m")

  def fgcolor(code: Color) =
    color("38", code.n)

  def bgcolor(code: Color) =
    color("48", code.n)

  def styled(text: String, style: Style): String =
    fgcolor(style.fg) + bgcolor(style.bg) + text

  def render(prompt: Seq[Segment]): String = prompt.map {
    case TextSegment(text, style) => styled(text, style)
    case SectionSeparator(text) => text
  }.mkString("") + reset

}



