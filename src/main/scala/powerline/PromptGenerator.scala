package powerline


trait PromptGenerator {

  def newPrompt(request: Request): Seq[Segment]

  def newPromptText(request: Request): String =
    drawToString(newPrompt(request))


  private def drawToString(segments: Seq[Segment]): String = {
    val shifted = segments.tail
    val output = (for {
      (curr, next) <- segments zip shifted
    } yield curr.draw(Some(next))) mkString ""

    val sb = StringBuilder.newBuilder
    output.foreach(sb.append)
    sb.append(segments.last.draw(None))

    sb.toString()
  }

}