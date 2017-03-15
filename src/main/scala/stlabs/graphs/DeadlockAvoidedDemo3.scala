package stlabs.graphs

import akka.stream._
import stlabs.BaseApp
import akka.stream.scaladsl._

object DeadlockAvoidedDemo3 extends BaseApp with App {
  import GraphDSL.Implicits._

  RunnableGraph.fromGraph(GraphDSL.create(){ implicit b =>

    val zip = b.add(ZipWith[Int, Int, Int]((left, right) => left))
    val bcast = b.add(Broadcast[Int](2))
    val concat = b.add(Concat[Int]())
    val start = Source.single(0)

    source ~> zip.in0
    zip.out.map{s => println(s); s} ~> bcast ~> Sink.ignore
    zip.in1 <~ concat <~ start
    concat <~ bcast

    ClosedShape
  }).run()

}
