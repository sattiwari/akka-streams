package stlabs.graphs

import akka.stream.{ClosedShape, OverflowStrategy}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, RunnableGraph, Sink}
import stlabs.BaseApp

object DeadlockAvoidedDemo2 extends BaseApp with App {

  RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._

    val merge = b.add(Merge[Int](2))
    val bcast = b.add(Broadcast[Int](2))

    source ~> merge ~> Flow[Int].map { s => println(s); s } ~> bcast ~> Sink.ignore
    merge <~ Flow[Int].buffer(10, OverflowStrategy.dropHead) <~ bcast
    ClosedShape
  }).run()

//  Thread.sleep(1000)
//  system.shutdown()
}
