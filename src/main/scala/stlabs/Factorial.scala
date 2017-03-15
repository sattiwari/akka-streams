package stlabs

import java.io.File

import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Flow, Keep, Sink}
import akka.util.ByteString

import scala.concurrent.Future

object Factorial extends App with BaseApp {

//  accepts string as input and when materialized creates IOResult as aux info
  def lineSink(fileName: String): Sink[String, Future[IOResult]] =
    Flow[String]
      .map(num => ByteString(s"${num}\n"))
      .toMat(FileIO.toFile(new File(fileName)))(Keep.right) //connects flow to a string

  factorial.map(_.toString).runWith(lineSink("factorial.txt"))

  shutdown()
}
