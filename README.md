# Grupo de lectura de concurrencia y paralelismo

>Proponer alguno de :

> * Un post de un blog
> * Un capítulo o un segmento de un libro
> * Un video de una conferencia o una presentación
> * Un pedazo de código fuente

> que no tome más de una hora consumir y que trate sobre temas de concurrencia o paralelismo. Reunirnos a discutir, cada semana, qué entendemos y qué no durante máximo una hora. Los temas deben ser autocontenidos y cualquier persona puede unirse en cualquier momento o faltar a cualquiera de las sesiones.

## Sesiones


| Fecha     | Notas                                                                                          |
| --------- | ---------------------------------------------------------------------------------------------- |
|19/04/2016 | [Jessica Kerr's "Concurrency options on the JVM"](https://github.com/miguel-vila/grupo-concurrencia-paralelismo/blob/master/19-04-2016-concurrency-options-on-the-jvm.md) |
|26/04/2016 | [¿Cómo funcionan los futuros de la librería estándar de Scala?](https://github.com/miguel-vila/grupo-concurrencia-paralelismo/blob/master/26-04-2016-scala-std-futures.md#cómo-funcionan-los-futuros-de-la-librería-estándar-de-scala)

## Posibles Temas

* API de `ExecutorService`s de la JVM.:
	* [ExecutorService](http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ExecutorService.html)
	* [ThreadPoolExecutor](http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ThreadPoolExecutor.html)
	* [Choosing an ExecutorService](http://blog.jessitron.com/2014/01/choosing-executorservice.html)
	* [Fork Join Pool](http://blog.jessitron.com/2014/02/forkjoinpool-other-executorservice.html)

* _Pools_ de _threads_:
	* [Thread pools! How do I use them?](http://jvns.ca/blog/2016/03/27/thread-pools-how-do-i-use-them/)
	* [I conquered thread pools! For today, at least.](http://jvns.ca/blog/2016/03/29/thread-pools-part-ii-i-love-blocking/)

* [Opciones de concurrencia en la JVM](https://www.youtube.com/watch?v=yhguOt863nw)

* Algunos videos de [este curso](https://www.udacity.com/course/introduction-to-operating-systems--ud923)

* [Parallelism and concurrency need different tools](http://yosefk.com/blog/parallelism-and-concurrency-need-different-tools.html)

* ¿Cómo funcionan los [`Future`](https://github.com/scala/scala/blob/7910508d1071e0e769ff6e291d3a1c479a067262/src/library/scala/concurrent/Future.scala)s de la librería estándar de Scala?

* ¿Como funcionan los [`Task`](https://github.com/scalaz/scalaz/blob/series/7.3.x/concurrent/src/main/scala/scalaz/concurrent/Task.scala)s y [`Future`](https://github.com/scalaz/scalaz/blob/series/7.3.x/concurrent/src/main/scala/scalaz/concurrent/Future.scala)s de scalaz?

* ¿Cómo funciona [Reactive Extensions](https://github.com/Reactive-Extensions/)?

* Actores:
	* [Modelo de actores](http://danielwestheide.com/blog/2013/02/27/the-neophytes-guide-to-scala-part-14-the-actor-approach-to-concurrency.html)
	* [Actors are not a good concurrency model](http://pchiusano.blogspot.com.co/2010/01/actors-are-not-good-concurrency-model.html)

* [Communicating Sequential Processes](https://en.wikipedia.org/wiki/Communicating_sequential_processes).

* Concurrencia en JavaScript:
	* [Philip Roberts: Help, I’m stuck in an event-loop.](https://vimeo.com/96425312)
	* [Concurrency model and Event Loop](Concurrency model and Event Loop)
	*  [Understanding the node.js event loop](http://blog.mixu.net/2011/02/01/understanding-the-node-js-event-loop/)
	* [Reactor Pattern](http://www.cs.wustl.edu/~schmidt/PDF/reactor-siemens.pdf)

* Algún capítulo de [Java Concurrency in Practice](https://www.google.com.co/search?q=Java+Concurrency+in+Practice+pdf+download)
