package powerline

/**
 * One segment of a prompt
 */
trait Segment {
  def draw(next: Option[Segment]): String
}

