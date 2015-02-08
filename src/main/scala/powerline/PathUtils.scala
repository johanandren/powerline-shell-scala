package powerline

import java.io.File

object PathUtils {

  /**
   * @return true if path was truncated, and then the sequence of path parts, possibly truncated from the left
   */
  def cwdString(path: File, home: File, maxLen: Int): (Boolean, Seq[String]) = {
    val dirs = path.getAbsolutePath match {
      case msg if msg.startsWith(home.getAbsolutePath) =>
        msg.replaceFirst(home.getAbsolutePath, "~").split("/").toIndexedSeq
      case "/" =>
        IndexedSeq("/")
      case msg =>
        msg.substring(1).split("/").toIndexedSeq  // remove the leading "/"
    }

    var len = dirs.foldLeft(0)((l, d) => l + d.length + 3)
    val hasDrop = len > maxLen

    val (firsts, last) = (dirs.slice(0, dirs.length-1), dirs.last)

    val uuy = firsts.dropWhile { dir: String =>
      val drop = len > maxLen
      len = len - dir.length - 3
      drop
    }

    (hasDrop, uuy)
  }


}
