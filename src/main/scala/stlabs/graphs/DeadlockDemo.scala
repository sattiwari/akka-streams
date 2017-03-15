package stlabs.graphs

import akka.stream.ClosedShape
import akka.stream.scaladsl._

object DeadlockDemo {

  val source = Source(List(1,2,3))

  RunnableGraph.fromGraph(GraphDSL.create() { implicit b =>
    import GraphDSL.Implicits._

    val merge = b.add(Merge[Int](2))
    val bcast = b.add(Broadcast[Int](2))

    source ~> merge ~> Flow[Int].map { s => println(s); s } ~> bcast ~> Sink.ignore
    merge                    <~                      bcast
    ClosedShape
  })

}
