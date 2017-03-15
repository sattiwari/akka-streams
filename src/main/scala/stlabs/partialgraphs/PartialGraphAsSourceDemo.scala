package stlabs.partialgraphs

import akka.stream.SourceShape
import akka.stream.scaladsl.{GraphDSL, Sink, Source, Zip}
import stlabs.BaseApp

import scala.concurrent.Await
import scala.concurrent.duration._

object PartialGraphAsSourceDemo extends App with BaseApp {

  val pairs = Source.fromGraph(GraphDSL.create(){ implicit b =>
    import GraphDSL.Implicits._

    val zip = b.add(Zip[Int, Int]())
    val ints = Source.fromIterator(() => Iterator.from(1))

    ints.filter(_ % 2 != 0) ~> zip.in0
    ints.filter(_ % 2 == 0) ~> zip.in1

    SourceShape(zip.out)
  })

  val firstPair = pairs.runWith(Sink.head)
  println(Await.result(firstPair, 300 millis))

  system.shutdown()
}
