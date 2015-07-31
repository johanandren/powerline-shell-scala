package powerline.vcs


trait Repository {
  def close(): Unit
  def status: RepoStatus
}