package stlabs

import akka.stream.scaladsl._

object NestingDemo extends App with BaseApp {

  Source.single(0)
    .map(_ + 1)
    .filter(_ != 0)
    .map(_ - 2)
    .to(Sink.fold(0)(_ + _))
    .run()

  val nestedSource =
    Source.single(0) // An atomic source
      .map(_ + 1) // an atomic processing stage
      .named("nestedSource") // wraps up the current Source and gives it a name

  val nestedFlow =
  Flow[Int].filter(_ != 0) // an atomic processing stage
    .map(_ - 2) // another atomic processing stage
    .named("nestedFlow") // wraps up the Flow, and gives it a name

  val nestedSink =
  nestedFlow.to(Sink.fold(0)(_ + _)) // wire an atomic sink to the nestedFlow
    .named("nestedSink") // wrap it up

  // Create a RunnableGraph
  val runnableGraph = nestedSource.to(nestedSink)


}
