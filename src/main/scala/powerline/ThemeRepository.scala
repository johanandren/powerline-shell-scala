package powerline

import akka.actor.SupervisorStrategy._
import akka.actor.{OneForOneStrategy, SupervisorStrategy, Actor, Props}

import scala.util.{Failure, Success}

object ThemeRepository {

  case class GetTheme(name: String)
  case class ThemeResult(theme: Theme)

  def props = Props(new ThemeRepository)
}

class ThemeRepository extends Actor {

  val defaultTheme = "darcula"

  import ThemeRepository._

  var themeCache = Map.empty[String, (Long, Theme)]

  def receive = {
    case GetTheme(name) if themeCache.contains(name) =>


    case GetTheme(name) =>
      Theme.load(name).orElse(Theme.load(defaultTheme)) match {
        case Success(theme) => sender() ! ThemeResult(theme)
        case Failure(ex) => throw ex
      }
  }
}


object ThemeSupervisor {

  def props = Props(new ThemeSupervisor)
}

class ThemeSupervisor extends Actor {

  val themes = context.actorOf(ThemeRepository.props, "repo")

  def receive = {
    case msg => themes forward msg
  }

  override def supervisorStrategy: SupervisorStrategy = {
    val strategy: Decider = {
      case _: RuntimeException => Restart
    }
    OneForOneStrategy()(strategy orElse super.supervisorStrategy.decider)
  }

}