import zio._
import zio.logging.backend.SLF4J

import scala.language.postfixOps


object NotActuallySemaphoresPar extends ZIOAppDefault {
  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  override def run: ZIO[Environment with ZIOAppArgs with Scope, Any, Any] = {
    val as = 1 to 100
    for {
      ref <- Ref.make(Seq.empty[Int])
      f <- ZIO.foreachParDiscard(as)(a => ref.update(_ :+ a).delay(1.seconds))
        .withParallelism(2).fork
      f2 <- ref.get.flatMap(i => ZIO.logInfo(i.mkString(" "))).schedule(Schedule.fixed(1.seconds)).fork
      _ <- f.join
      _ <- f2.join
    } yield ()
  }
}
