package powerline.vcs

import akka.actor.Actor.Receive
import akka.actor.{Props, Actor}

object RepositySupervisor {
  def props = Props(new RepositySupervisor)
}

class RepositySupervisor extends Actor {

  val repository = context.actorOf(Repositories.props, "repositories")

  override def receive: Receive = {
    case x => repository forward x
  }
}
