package stlabs

import akka.stream.scaladsl._
import scala.concurrent.duration._

object BatchMessageDemo extends App {

  /*
  Simulate an infinite stream of samples and write them to a database 1000 elements at a time
   */
//  Source.tick(0 milliseconds, 10 milliseconds, ())
//    .map(_ => Sample(System.currentTimeMillis(), random.nextFloat()))
//    .groupedWithin(1000, 100 milliseconds)
//    .map(database.bulkInsert)
//    .runWith(Sink.ignore)

}
