package powerline

import java.io.File

import akka.actor.{ActorLogging, Props, Actor}
import scala.collection.immutable.TreeMap
import scala.concurrent.duration._
import scala.io.Source

object DirectoryHistory {
  case class DirectoryVisited(path: File)
  case class Query(query: String)
  case class Result(path: Option[File])
  case object GetLastDirectory
  case class LastDirectory(directory: Option[File])


  def props = Props(new DirectoryHistory)


  private val msInADay = 1000 * 60 * 60 * 24

  case class DirectoryEntry(path: File, visits: Int, lastVisit: Long) {
    def visit = this.copy(visits = visits + 1, lastVisit = System.currentTimeMillis())

    lazy val score: Long = {
      // score algo that balances how recently it was visited and how many times
      val daysSinceLastVisit = (System.currentTimeMillis - lastVisit) / msInADay
      (visits.toDouble / math.max(1, daysSinceLastVisit)).toLong
    }
  }

  private implicit val entryOrdering = Ordering.by[DirectoryEntry, Long](_.score)

  def search(query: String, files: Iterable[File]): Option[File] = {
    val lcQuery = query.toLowerCase
    files.find(matchesExactly(lcQuery, _)).orElse(
      files.find(startsWith(lcQuery, _))
    ).orElse(
      files.find(contains(lcQuery, _))
    )
  }

  def matchesExactly(lcQuery: String, file: File): Boolean =
    file.getName.toLowerCase == lcQuery


  def startsWith(lcQuery: String, file: File): Boolean =
    file.getName.toLowerCase.startsWith(lcQuery)

  def contains(lcQuery: String, file: File): Boolean =
    file.getName.toLowerCase.contains(lcQuery)


}

class DirectoryHistory(maxHistorySize: Int = 60) extends Actor with ActorLogging {

  import DirectoryHistory._

  case object PruneHistory
  import context.dispatcher

  // we don't persist this, it's just to know what directory we last entered
  // for example for going there in a new tab
  var lastDirectory: Option[File] = None

  val cancelable = context.system.scheduler.schedule(1 minute, 1 minute, self, PruneHistory)

  var history = TreeMap.empty[File, DirectoryEntry] withDefault (file => DirectoryEntry(file, 0, 0))


  override def receive: Receive = {

    case DirectoryVisited(path) =>
      lastDirectory = Some(path)
      history = history + (path -> history(path).visit)
      pruneHistory()

    case PruneHistory =>
      pruneHistory()
      save()

    case Query(query) =>
      val result = search(query, history.values.map(_.path))
      sender() ! Result(result)
      log.info(s"Search for $query found $result")

    case GetLastDirectory =>
      sender() ! LastDirectory(lastDirectory)

  }



  def pruneHistory(): Unit = {
    if (history.size > maxHistorySize) {
      history = history.take(maxHistorySize)
    }
  }


  def historyFile = new File(System.getenv("HOME"), ".q_history")
  override def preStart(): Unit = {
    val file = historyFile
    if (file.exists()) {
      val source = Source.fromFile(file)

      history = history ++ source.getLines().map { line =>
        val parts = line.split('|')
        val entry = DirectoryEntry(new File(parts(0)), parts(1).toInt, parts(2).toLong)
        entry.path -> entry
      }.toMap

      source.close()
    }
  }

  override def postStop(): Unit = {
    save()
  }

  def save(): Unit = {
    val file = historyFile
    if (file.exists()) file.delete()
    file.createNewFile()
    val asString = history.values.map(entry => s"${entry.path.getAbsolutePath}|${entry.visits}|${entry.lastVisit}").mkString("\n")
    val p = new java.io.PrintWriter(file)
    try { p.write(asString) } finally { p.close() }
  }
}
