package stlabs

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source

trait BaseApp {
  implicit val system = ActorSystem("sys")

  //  it is factory for stream execution engine. It makes streams run.
  implicit val materializer = ActorMaterializer()

  val source: Source[Int, NotUsed] = Source(1 to 100)

  //  source can be reused
  lazy val factorial = source.scan(BigInt(1))((acc, next) => next * acc)

  def shutdown(): Unit = {
    Thread.sleep(100)
    system.shutdown()
  }
}
