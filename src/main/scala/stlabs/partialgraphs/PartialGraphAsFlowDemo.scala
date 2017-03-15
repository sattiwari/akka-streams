package stlabs.partialgraphs

import akka.stream.FlowShape
import akka.stream.scaladsl._
import stlabs.BaseApp

import scala.concurrent.Await
import scala.concurrent.duration._

object PartialGraphAsFlowDemo extends App with BaseApp {

  val pairUpWithToString =
    Flow.fromGraph(GraphDSL.create(){ implicit b =>
      import GraphDSL.Implicits._

      val bcast = b.add(Broadcast[Int](2))
      val zip = b.add(Zip[Int, String]())

      bcast.out(0).map(identity) ~> zip.in0
      bcast.out(1).map(_.toString) ~> zip.in1

      FlowShape(bcast.in, zip.out)
    })

  val res = pairUpWithToString.runWith(Source(List(1)), Sink.head)

  println(Await.result(res._2, 300 millis))

  system.shutdown()
}
