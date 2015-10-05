package powerline.vcs

// public model
case class RepoStatus(
    dirty: Boolean,
    behind: Int,
    ahead: Int,
    label: String,
    hasRemote: Boolean,
    detached: Boolean)