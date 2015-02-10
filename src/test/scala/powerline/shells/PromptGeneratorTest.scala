package powerline.shells

import java.io.File

import org.scalatest.{FlatSpec, Matchers}
import powerline.{Segment, AppConfig, Theme}

class PromptGeneratorTest extends FlatSpec with Matchers {

  val theme = new Theme
  val generator = new PromptGenerator(new AppConfig(
    theme = theme
  ))

  "The generator" should "represent home with a symbol" in {
    val prompt = generator.pathSegments(new File("/User/goat"), 120, new File("/User/goat"))

    prompt should have size 1
    val segment = prompt.head

    segment.content should be (" " + theme.homeSymbol + " ")
  }

  it should "represent paths under home with a relative path" in {
    val prompt = generator.pathSegments(new File("/User/goat/code"), 120, new File("/User/goat"))

    prompt should have size 3

    val home= prompt(0)
    home.content should be (theme.homeSymbol)

    val separator = prompt(1)
    separator.content should be (theme.dirSeparator)

    val dir = prompt(2)
    dir.content should be (" code ")
  }
}
