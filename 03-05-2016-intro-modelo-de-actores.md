# [John Murray's "Introduction to the Actor Model for Concurrent Computation"](https://www.youtube.com/watch?v=lPTqcecwkJg)

## Actors model

A method of concurrency in which the universal primitive is the actor.

### Properties

* Actors are persistent (they are alive): they exist whether they are doing something or not.
* Encapsulate internal state: It is encapsulated unlike say threads.
* Actors are asynchronous

### What actors can do

* Create other actors
* Receive messages and in response
  * make local decisions (e.g. alter local state)
  * perform arbitratry, side-effect action (write to a db)
  * send messages
  * respond to the sender 0 or more times
* Process exactly one message at a time: "inside an actor there is no aditional concurrency"

> Do not communicate by sharing memory; instead share memory by communicating - Effective go

## Example: checking account

```scala
class Checking extends Actor {

  var balance = 80

  def receive = {
    case Withadral(amt) =>
      if(balance > amt) {
        balance -= amt
        sender ! true
      } else {
        sender ! false
      }
  }

}
```

No problem with concurrent withadrals:

```scala
val checking: ActorRef = ...

// at the same time:
val response1: Future[Boolean] = ( checking ? Withadral(60) ).mapTo[Boolean]
val response2: Future[Boolean] = ( checking ? Withadral(50) ).mapTo[Boolean]
```

### Properties of communications

* No channels or intermediaries (unlike CSP)
* "Best effort" delivery
* At-most-once delivery (no retries, no timeouts, nothing other than the underlying protocol)
* Messsages can take arbitrary long to be delivered
* No messsage ordering guarantees

### Address (`ActorRef` in Akka)

* Identifies an actor
* May also represent a proxy / forwarder to an actor
* Contains location and transport information
* Location transparency -> distributable by default
* One actor may have many different addresses
* One address may represent multiple actors (e.g. load balancing)

### Handling failure

Supervision: the running state of an actor is monitored and managed.

* Properties:
  * Constantly monitors running state of actor
  * Can perform actions based on the state of the actor: e.g. start over

* Supervision Trees: hierarchies

* Transparent Lifecycle management:
  * Addresses don't change during restarts
  * Mailboxes are persisted outside the actor instances: Address encapsulates both the mailbox and the actor

## Use cases

* Processsing pipeline (?)
* Streaming data (?)
* Multi-user concurrency: playing with actors liveness -> e.g. have an actor for each user and each event by the user is a message to the actor
* Systems with high uptime requirements (Ericsson's Erlang )
* Applications with shared state
* Batch Job Processing
* Breaking up the work:
  * First layer of HTTP actors
  * Work queue
  * Job Processors: has child actors and tracks jobs
  * Allows to controll level of concurrency.
  * Client pushes work, workers pull from queue.

## Demo

Pool of workers. Job: identify prime numbers in a range. (Why workers are useful for this? Why not with futures?)

## Anti Use-cases

* When there is no concurrency
* It's a high level abstraction. Bad for performance?
* No mutable state (but can't it be useful for distribution even if there is no state?)

## Drawbacks

* Too many actors: hard for tracing, understanding or testing
* Debugging

## ¿Como funciona en Pony? (Para contrastar)

[Pony-lang](http://www.ponylang.org/) es un lenguaje que soporta actores y tiene un acercamiento distinto al de scala. Específicamente permite definir los manejos de los mensajes mediante _behaviours_. En el siguiente actor el _behaviour_ `eat` es uno de los mensajes que el actor puede recibir y se define de forma similar a una función:

```pony
actor Aardvark
  let name: String
  var _hunger_level: U64 = 0

  new create(name': String) =>
    name = name'

  be eat(amount: U64) =>
    _hunger_level = _hunger_level - amount.min(_hunger_level)
```

lo anterior permite que el _typechecker_ verifique que los mensajes que se le envíen a un actor sean los que de verdad es capaz de manejar. A diferencia en Akka este tipo de errores no son atrapados sino hasta cuando se está ejecutando el programa. Sin embargo Akka agregó algo de actores tipados que no revisamos.

## Pendientes

* Ver _dispatchers_ y de qué sirven
* ¿Cómo están implementados?
