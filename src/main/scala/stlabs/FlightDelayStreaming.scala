package stlabs

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, Source}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scala.util.Try



object FlightDelayStreaming extends App {

  implicit val system = ActorSystem("sys")
  implicit val materializer = ActorMaterializer()

  val flightDelayLines = io.Source.fromFile("src/main/resources/1987.csv", "utf-8").getLines()

  val g = RunnableGraph.fromGraph(GraphDSL.create(){
    implicit builder =>
      import GraphDSL.Implicits._

//      Source
      val A = builder.add(Source.fromIterator(() => flightDelayLines)).out

//      flows
      val B: FlowShape[String, FlightEvent] = builder.add(csvToFlightEvent)
//      val C: FlowShape[FlightEvent, FlightDelayRecord] = builder.add(filterAndConvert)
      val D: UniformFanOutShape[FlightDelayRecord, FlightDelayRecord] = builder.add(Broadcast[FlightDelayRecord](2))
//      val F: FlowShape[FlightDelayRecord, (String, Int, Int)] = builder.add(avgCarrierDelay)

//      sinks
      val E: Inlet[Any] = builder.add(Sink.ignore).in
      val G: Inlet[Any] = builder.add(Sink.foreach(averageSink)).in

//      Graph
//      A ~> B ~> C ~> D
//                E <~ D
//           G <~ F <~ D

      A ~> G

      ClosedShape
  })

  g.run()

  def averageSink[A](a: A): Unit = {
    a match {
      case (a: String, b: Int, c: Int) => println(s"Delays for carrier ${a}: ${Try(c / b).getOrElse(0)} average mins, ${b} delayed flights")
      case x => println(s"no idea about ${x}")
    }
  }

  def csvToFlightEvent = Flow[String]
      .map(_.split(",").map(_.trim))
      .map(stringArrayToFlightEvent)

  def stringArrayToFlightEvent(cols: Array[String]) = new FlightEvent(cols(0), cols(1), cols(2), cols(3), cols(4), cols(5),
    cols(6), cols(7), cols(8), cols(9), cols(10), cols(11), cols(12), cols(13), cols(14), cols(15), cols(16), cols(17), cols(18),
    cols(19), cols(20), cols(21), cols(22), cols(23), cols(24), cols(25), cols(26), cols(27), cols(28))

//  transform flight events to delay records (only for records with a delay)
//  val filterAndConvert = Flow[FlightEvent]
//    .filter(r => Try(r.arrDelayMins.toInt).getOrElse(-1) > 0) //convert arrival delays to int; filter non delays
//    .mapAsyncUnordered(parallelism = 2) { r =>
//      Future(new FlightDelayRecord(r.year, r.month, r.dayOfMonth, r.flightNum, r.uniqueCarrier, r.arrDelayMins))
//    }

  val filterAndConvert =
    Flow[FlightEvent]
      .filter(r => Try(r.arrDelayMins.toInt).getOrElse(-1) > 0) // convert arrival delays to ints, filter out non delays
      .mapAsyncUnordered(parallelism = 2) { r =>
      Future(new FlightDelayRecord(r.year, r.month, r.dayOfMonth, r.flightNum, r.uniqueCarrier, r.arrDelayMins))
    }

  val avgCarrierDelay = Flow[FlightDelayRecord]
    .groupBy(30, _.uniqueCarrier)
    .fold(("", 0, 0)) {
      (x: (String, Int, Int), y: FlightDelayRecord) =>
        val count = x._2 + 1
        val totalMins = x._3 + Try(y.arrDelayMins.toInt).getOrElse(0)
        (y.uniqueCarrier, count, totalMins)
    }.mergeSubstreams


}

case class FlightEvent(
                        year: String,
                        month: String,
                        dayOfMonth: String,
                        dayOfWeek: String,
                        depTime: String,
                        scheduledDepTime: String,
                        arrTime: String,
                        scheduledArrTime: String,
                        uniqueCarrier: String,
                        flightNum: String,
                        tailNum: String,
                        actualElapsedMins: String,
                        crsElapsedMins: String,
                        airMins: String,
                        arrDelayMins: String,
                        depDelayMins: String,
                        originAirportCode: String,
                        destinationAirportCode: String,
                        distanceInMiles: String,
                        taxiInTimeMins: String,
                        taxiOutTimeMins: String,
                        flightCancelled: String,
                        cancellationCode: String, // (A = carrier, B = weather, C = NAS, D = security)
                        diverted: String, // 1 = yes, 0 = no
                        carrierDelayMins: String,
                        weatherDelayMins: String,
                        nasDelayMins: String,
                        securityDelayMins: String,
                        lateAircraftDelayMins: String)

case class FlightDelayRecord(
                              year: String,
                              month: String,
                              dayOfMonth: String,
                              flightNum: String,
                              uniqueCarrier: String,
                              arrDelayMins: String) {
  override def toString = s"${year}/${month}/${dayOfMonth} - ${uniqueCarrier} ${flightNum} - ${arrDelayMins}"
}
