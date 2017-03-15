# akka-streams

## Concepts
* Source, Sink, Flow

## Graphs
* Partial graphs
* Partial graph as a source
* Partial graph as a flow
* Closed graphs
* Materialized value of a graph
* Deadlock

## Shapes
* Custom Shape
* Bi-directional flow

## Examples
* Build a protocol stack which is composed of two stages - codec stage and framing stage. Codec stage serializes outgoing messages and deserializes incoming octet streams.
Framing stage could add a framing protocol that attaches a length header to outgoing data and parses incoming frames back into the original octet stream chunks. These two stages are meant
to be composed, applying one atop the other as part of a protocol stack.

* Deadlock - The graph takes elements from the source, prints them, then broadcasts those elements to a sink and to a feedback arc that is merged back into the main stream via a Merge junction.