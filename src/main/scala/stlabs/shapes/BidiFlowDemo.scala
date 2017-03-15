package stlabs.shapes

import java.nio.ByteOrder

import akka.stream._
import akka.stream.scaladsl._
import akka.stream.stage._
import akka.util.ByteString
import stlabs.BaseApp

import scala.concurrent.Await
import scala.concurrent.duration._

trait Message
case class Ping(id: Int) extends Message
case class Pong(id: Int) extends Message

//a graph that has exactly two open inlets and two open outlets
object BidiFlowDemo extends App with BaseApp {

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

  val framing = BidiFlow.fromGraph(GraphDSL.create() { b =>
    implicit val order = ByteOrder.LITTLE_ENDIAN

    def addLengthHeader(bytes: ByteString) = {
      val len = bytes.length
      ByteString.newBuilder.putInt(len).append(bytes).result()
    }

    class FrameParser extends GraphStage[FlowShape[ByteString, ByteString]] {

      val in = Inlet[ByteString]("FrameParser.in")
      val out = Outlet[ByteString]("FrameParser.out")
      override val shape = FlowShape.of(in, out)

      override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

        // this holds the received but not yet parsed bytes
        var stash = ByteString.empty
        // this holds the current message length or -1 if at a boundary
        var needed = -1

        setHandler(out, new OutHandler {
          override def onPull(): Unit = {
            if (isClosed(in)) run()
            else pull(in)
          }
        })
        setHandler(in, new InHandler {
          override def onPush(): Unit = {
            val bytes = grab(in)
            stash = stash ++ bytes
            run()
          }

          override def onUpstreamFinish(): Unit = {
            if (stash.isEmpty) completeStage()
            // wait with completion and let run() complete when the
            // rest of the stash has been sent downstream
          }
        })

        private def run(): Unit = {
          if (needed == -1) {
            // are we at a boundary? then figure out next length
            if (stash.length < 4) {
              if (isClosed(in)) completeStage()
              else pull(in)
            } else {
              needed = stash.iterator.getInt
              stash = stash.drop(4)
              run() // cycle back to possibly already emit the next chunk
            }
          } else if (stash.length < needed) {
            // we are in the middle of a message, need more bytes,
            // or have to stop if input closed
            if (isClosed(in)) completeStage()
            else pull(in)
          } else {
            // we have enough to emit at least one message, so do it
            val emit = stash.take(needed)
            stash = stash.drop(needed)
            needed = -1
            push(out, emit)
          }
        }
      }
    }

    val outbound = b.add(Flow[ByteString].map(addLengthHeader))
    val inbound = b.add(Flow[ByteString].via(new FrameParser))
    BidiShape.fromFlows(outbound, inbound)
  })

  val stack = codec.atop(framing)

  // test it by plugging it into its own inverse and closing the right end
  val pingpong = Flow[Message].collect { case Ping(id) => Pong(id) }
  val flow = stack.atop(stack.reversed).join(pingpong)
  val result = Source((0 to 9).map(Ping)).via(flow).limit(20).runWith(Sink.seq)
  val x = Await.result(result, 1.second)

  x.foreach(println)

  system.shutdown()
}
