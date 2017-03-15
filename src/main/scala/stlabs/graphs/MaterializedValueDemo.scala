package stlabs.graphs

import akka.stream.{FlowShape, SourceShape}
import akka.stream.scaladsl._

import scala.concurrent.Future

object MaterializedValueDemo {
  import GraphDSL.Implicits._

  val foldFlow: Flow[Int, Int, Future[Int]] = Flow.fromGraph(GraphDSL.create(Sink.fold[Int, Int](0)(_ + _)) { implicit builder => fold =>
    FlowShape(fold.in, builder.materializedValue.mapAsync(4)(identity).outlet)
  })

//  cyclic fold demo
  val cyclicFold: Source[Int, Future[Int]] = Source.fromGraph(GraphDSL.create(Sink.fold[Int, Int](0)(_ + _)) { implicit builder => fold =>
    // - Fold cannot complete until its upstream mapAsync completes
    // - mapAsync cannot complete until the materialized Future produced by
    //   fold completes
    // As a result this Source will never emit anything, and its materialited
    // Future will never complete
    builder.materializedValue.mapAsync(4)(identity) ~> fold
    SourceShape(builder.materializedValue.mapAsync(4)(identity).outlet)
  })


}
