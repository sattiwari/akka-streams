package stlabs

import akka.Done
import akka.stream.ThrottleMode
import akka.stream.scaladsl.Source

import scala.concurrent.Future
import scala.concurrent.duration._

object TimeBasedProcessingDemo extends App with BaseApp {

  val done: Future[Done] =
    factorial
      .zipWith(Source(0 to 100))((elem, idx) => s"${idx}! = ${elem}")
      .throttle(1, 1 second, 1, ThrottleMode.shaping) // sends downstream messages at 1 elem/sec
      .runForeach(println)

}
