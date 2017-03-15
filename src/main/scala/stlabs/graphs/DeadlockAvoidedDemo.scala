package stlabs.graphs

import akka.stream.ClosedShape
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, MergePreferred, RunnableGraph, Sink}
import stlabs.BaseApp

object DeadlockAvoidedDemo extends BaseApp with App {

  RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._

    val merge = b.add(MergePreferred[Int](1))
    val bcast = b.add(Broadcast[Int](2))

    source ~> merge ~> Flow[Int].map { s => println(s); s } ~> bcast ~> Sink.ignore
    merge.preferred                    <~                      bcast
    ClosedShape
  }).run()

  Thread.sleep(1000)
  system.shutdown()
}

