package powerline

import java.io.File

object FileSystem {

  def isInside(file: File, somePath: File): Boolean =
    file.getAbsolutePath.startsWith(somePath.getAbsolutePath)


}
