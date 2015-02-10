package powerline.shells

import powerline.Segment

trait PromptRenderer extends (Seq[Segment] => String)
