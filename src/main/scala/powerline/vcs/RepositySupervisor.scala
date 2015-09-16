package powerline.vcs

import akka.actor.Actor.Receive
import akka.actor.SupervisorStrategy.{Decider, Restart}
import akka.actor.{OneForOneStrategy, SupervisorStrategy, Props, Actor}

object RepositySupervisor {
  def props = Props(new RepositySupervisor)
}

class RepositySupervisor extends Actor {

  val repository = context.actorOf(Repositories.props, "repositories")

  override def receive: Receive = {
    case x => repository forward x
  }

  override def supervisorStrategy: SupervisorStrategy = {
    val strategy: Decider = {
      case _: RuntimeException =>
        sender() ! Repositories.Result(None)
        Restart
    }
    OneForOneStrategy()(strategy orElse super.supervisorStrategy.decider)
  }
}
