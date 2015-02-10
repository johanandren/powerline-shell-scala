package powerline

import com.typesafe.config.ConfigFactory

object Theme {

  def load(name: String): Theme = {
    val config = ConfigFactory.load(s"$name")
    val colorConfig = config.getConfig("theme.colors")

    def style(fgKey: String, bgKey: String) =
      Style(Color(colorConfig.getInt(fgKey)), Color(colorConfig.getInt(bgKey)))

    Theme(
      user = style("user.fg", "user.bg"),
      path = style("path.fg", "path.bg"),
      cwd = style("cwd.fg", "cwd.bg"),
      separator = style("separator.fg", "separator.bg"),
      repoClean = style("repo.clean.fg", "repo.clean.bg"),
      repoDirty = style("repo.dirty.fg", "repo.dirty.bg"),
      cmdPassed = style("lastCmd.passed.fg", "lastCmd.passed.bg"),
      cmdFailed = style("lastCmd.failed.fg", "lastCmd.failed.bg")
    )

  }

}

case class Theme(
    user: Style,
    path: Style,
    cwd: Style,
    separator: Style,
    repoClean: Style,
    repoDirty: Style,
    cmdPassed: Style,
    cmdFailed: Style)


case class Color(n: Int) extends AnyVal
case class Style(fg: Color, bg: Color)
