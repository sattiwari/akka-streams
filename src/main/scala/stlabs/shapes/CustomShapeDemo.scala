package stlabs.shapes

import akka.stream._
import stlabs.BaseApp

import scala.collection.immutable

object CustomShapeDemo extends App with BaseApp {

  // A shape represents the input and output ports of a reusable
  // processing module
  case class PriorityWorkerPoolShape[In, Out](jobsIn: Inlet[In], priorityJobsIn: Inlet[In], resultsOut: Outlet[Out]) extends Shape {

    // It is important to provide the list of all input and output
    // ports with a stable order. Duplicates are not allowed.
    override val inlets: immutable.Seq[Inlet[_]] = jobsIn :: priorityJobsIn :: Nil
    override val outlets: immutable.Seq[Outlet[_]] = resultsOut :: Nil

    // A Shape must be able to create a copy of itself. Basically
    // it means a new instance with copies of the ports
    override def deepCopy() = PriorityWorkerPoolShape(
      jobsIn.carbonCopy(),
      priorityJobsIn.carbonCopy(),
      resultsOut.carbonCopy())

    // A Shape must also be able to create itself from existing ports
    override def copyFromPorts(
                                inlets: immutable.Seq[Inlet[_]],
                                outlets: immutable.Seq[Outlet[_]]) = {
      assert(inlets.size == this.inlets.size)
      assert(outlets.size == this.outlets.size)
      // This is why order matters when overriding inlets and outlets.

      PriorityWorkerPoolShape[In, Out](inlets(0).as[In], inlets(1).as[In], outlets(0).as[Out])
    }
  }

}
