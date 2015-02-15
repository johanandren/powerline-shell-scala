package powerline.vcs

/**
 * All CVS status checking objects should implement
 * this interface
 */
trait VCSRepo {
  def clean: Boolean
  def dirty: Boolean = !clean
  def currentBranch: String
}
