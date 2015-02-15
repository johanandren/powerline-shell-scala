package powerline

class PathSpec extends Specification {

  val home = Seq("User","goat")

  "The path promptifyer" should "represent home with a symbol" in {
    val result = Path.promptify(home, home, 200)
    result should be (Seq("~"))
  }

  it should "represent paths under home with a relative path" in {
    val result = Path.promptify(home :+ "code", home, 200)
    result should be (Seq("~", "code"))
  }

  it should "represent root with just a slash" in {
    val result = Path.promptify(Seq(), home, 200)
    result should be (Seq("/"))
  }

  it should "non home paths with root first" in {
    val result = Path.promptify(Seq("usr", "local", "Cellar"), home, 200)
    result should be (Seq("/", "usr", "local", "Cellar"))
  }

  it should "shorten paths by dropping the left end" in {
    val result = Path.promptify(Seq("usr", "local", "libexec", "python"), home, 15)
    result should be (Seq("…", "libexec", "python"))
  }


}
