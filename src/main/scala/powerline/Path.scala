package powerline

import java.io.File

object Path {

  val homeSymbol = "~"

  def promptify(path: File, home: File, maxLength: Option[Int]): Seq[String] =
    promptify(
      path.getAbsolutePath.split('/').filter(_.nonEmpty),
      home.getAbsolutePath.split('/').filter(_.nonEmpty),
      maxLength
    )

  def promptify(path: Seq[String], home: Seq[String], maxLength: Option[Int]): Seq[String] = {
    val prompt = handleRoot(replaceHome(path, home))
    maxLength.fold(
      prompt
    )(maxLength =>
      shorten(prompt, maxLength)
    )
  }

  def replaceHome(path: Seq[String], home: Seq[String]): Seq[String] =
    if (path == home) Seq("~")
    else if (path.startsWith(home)) "~" +: path.drop(home.length)
    else path

  def handleRoot(path: Seq[String]): Seq[String] =
    if (path.isEmpty) Seq("/")
    else if (path.head != "~") "/" +: path
    else path

  def shorten(path: Seq[String], maxLength: Int): Seq[String] = {
    val possiblyTruncated = withRightToLeftAccLength(path).dropWhile(_._2 > maxLength).map(_._1)
    if (possiblyTruncated.length != path.length) "…" +: possiblyTruncated
    else possiblyTruncated
  }

  def withRightToLeftAccLength(words: Seq[String]): Seq[(String, Int)] =
    words.reverse.foldLeft(Seq[(String, Int)]()) { case (acc, part) =>
      val lengthWithThis = part.length + acc.lastOption.fold(0)(_._2)
      acc :+(part, lengthWithThis)
    }.reverse

}
