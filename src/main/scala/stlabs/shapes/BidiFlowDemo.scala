package stlabs.shapes

import java.nio.ByteOrder

import akka.stream.BidiShape
import akka.stream.scaladsl._
import akka.util.ByteString

trait Message
case class Ping(id: Int) extends Message
case class Pong(id: Int) extends Message

//a graph that has exactly two open inlets and two open outlets
object BidiFlowDemo {

  def toBytes(msg: Message): ByteString = {
    implicit val order = ByteOrder.LITTLE_ENDIAN
    msg match {
      case Ping(id) => ByteString.newBuilder.putByte(1).putInt(id).result()
      case Pong(id) => ByteString.newBuilder.putByte(2).putInt(id).result()
    }
  }

  def fromBytes(bytes: ByteString): Message = {
    implicit val order = ByteOrder.LITTLE_ENDIAN
    val it = bytes.iterator
    it.getByte match {
      case 1 => Ping(it.getInt)
      case 2 => Pong(it.getInt)
      case other => throw new RuntimeException(s"parse error: expected 1|2 got $other")
    }
  }

  val codecVerbose = BidiFlow.fromGraph(GraphDSL.create() { b =>
    // construct and add the top flow, going outbound
    val outbound = b.add(Flow[Message].map(toBytes))
    // construct and add the bottom flow, going inbound
    val inbound = b.add(Flow[ByteString].map(fromBytes))
    // fuse them together into a BidiShape
    BidiShape.fromFlows(outbound, inbound)
  })

  // this is the same as the above
  val codec = BidiFlow.fromFunctions(toBytes _, fromBytes _)

}
