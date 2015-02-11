package powerline

import java.io.File

import org.scalatest.{FlatSpec, Matchers}

class PromptGeneratorTest extends FlatSpec with Matchers {

  // one style to rule them all
  val s = Style(Color(15), Color(0))
  val theme = new Theme(s, s, s, s, s, s, s, s)
  val generator = new PromptGenerator(new AppConfig(
    theme = theme
  ))

  "The generator" should "represent home with a symbol" in {
    val prompt = generator.pathSegments(new File("/User/goat"), 120, new File("/User/goat"))

    prompt.segments should have size 1
    val segment = prompt.segments.head

    segment.text should be (" " + PromptGenerator.homeSymbol + " ")
  }

  it should "represent paths under home with a relative path" in {
    val prompt = generator.pathSegments(new File("/User/goat/code"), 120, new File("/User/goat"))

    prompt.segments should have size 3

    val home= prompt.segments(0)
    home.text should be (" " + PromptGenerator.homeSymbol + " ")

    val separator = prompt.segments(1)
    separator.text should be (PromptGenerator.dirSeparator)

    val dir = prompt.segments(2)
    dir.text should be (" code ")
  }
}
