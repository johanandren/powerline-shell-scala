package powerline.vcs

/**
 * All CVS status checking objects should implement
 * this interface
 */
trait VCSRepo {
  def isClean: Boolean
  def isDirty: Boolean = !isClean
  def currentBranch: String
}
