
import zio._
import zio.logging.backend.SLF4J

import scala.language.postfixOps


object SemaphoreExample extends ZIOAppDefault {
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] = Runtime.removeDefaultLoggers >>> SLF4J.slf4j
  var sharedVar: Int = 0

  override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] = for {
    semaphore <- Semaphore.make(1)
    _ <- ZIO.foreach((1 to 10).toList) { _ =>
      semaphore.withPermit(ZIO.succeed(sharedVar += 1)).delay(1.seconds)
    }.forkDaemon
    _ <- ZIO.logInfo(sharedVar.toString).schedule {
      Schedule.fixed(1.seconds) && Schedule.recurs(11)
    }
  } yield ()
}
