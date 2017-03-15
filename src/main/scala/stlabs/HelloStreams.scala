package stlabs

object HelloStreams extends App with BaseApp {

//  complements source with a consumer function & pass the stream setup to an actor that runs it
//  activation is signalled by run as part of method name
  source.runForeach(println)

  shutdown()

}
