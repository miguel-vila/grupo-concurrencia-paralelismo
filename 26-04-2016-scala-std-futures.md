# ¿Cómo funcionan los futuros de la librería estándar de Scala?

## Algunos "prerrequisitos"

La idea era que cada material fuera autocontenido, pero para entender cómo están implementados los futuros creo que mínimo toca saber para qué sirven y qué código nos permiten escribir. Creo que [este](http://danielwestheide.com/blog/2013/01/09/the-neophytes-guide-to-scala-part-8-welcome-to-the-future.html) es un buen artículo.

Además para entender la implementación hay que saber algo sobre el tipo `Try`. [Esta](http://danielwestheide.com/blog/2012/12/26/the-neophytes-guide-to-scala-part-6-error-handling-with-try.html) es una buena fuente.

## Material base

Código fuente (la versión más reciente a 19/04/16):

* Paquete `scala.concurrent`
  * [`Future` (`trait` del API público)](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/Future.scala)
  * [`Promise` (`trait` del API público)](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/Promise.scala)
* Paquete `scala.concurrent.impl` (Implementación):
  * [`Promise`](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/impl/Promise.scala)

El código fuente es un poco difícil de recorrer y entender.  Creo que uno entiende casi todo si se entienden los siguientes métodos:

* `map`
* `flatMap`
* `recover`
* `recoverWith`
* `Future.apply` (el método que se llama cuando uno hace `Future {...}`)
* `Future.successful`
* `Future.failed`
* `Future.sequence`
* `Future.sequence`

Primero creo que vale la pena entender el API público. `Future` representa un valor futuro (que puede ser exitoso o fallido con una excepción) y tiene combinadores funcionales como `map` o `flatMap` que permiten crear otros futuros. `Promise` es un objeto que puede ser completado con un valor o fallido con una excepción. La diferencia entre ambos es que `Future` es como el lado de lectura y `Promise` es el lado de escritura, dónde se puede asignar el valor. Un objeto de tipo `Promise[T]` tiene una referencia a un valor de tipo `Future[T]`, es decir a su lado de escritura. Esto resulta importante a la hora de ver como están implementados ciertos métodos.

## `Future` (API público)

`Future` es un `trait` que tiene algunos métodos abstractos y otros implementados en función de los abstractos, entre ellos los que nos interesan son estos:

| Método abstracto | Métodos que usan el método abstracto |
|------------------|--------------------------------------|
| [`transform`](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/Future.scala#L223-L231) | [`map`](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/Future.scala#L244-L264), [`recover`](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/Future.scala#L344-L361) |
| [`transformWith`](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/Future.scala#L233-L241) | [`flatMap`](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/Future.scala#L266-L280), [`recoverWith`](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/Future.scala#L363-L384) |
| [`onComplete`](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/Future.scala#L136-L149) | [`foreach`](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/Future.scala#L195-L205)

Es decir, si entendemos cómo están implementados `transform` y `transformWith` entendemos la funciones que nos interesan.

Además `Future` tiene un método abstracto [`value`](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/Future.scala#L163-L174) (¿Por qué en los comentarios dice que es no determinístico?) y otro [`isCompleted`](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/Future.scala#L154-L161).

## `Promise` (API público)

Por otra parte está `Promise` que tiene métodos para completar la promesa con un valor exitoso (un `Success(t)`) o con una excepción (un `Failure(throwable)`):

| Método abstracto | Métodos que usan el método abstracto |
| ---------------- | ------------------------------------ |
| [`tryComplete`](https://github.com/scala/scala/blob/2.12.x/src/library/scala/concurrent/Promise.scala#L51-L57) | [`complete`](https://github.com/scala/scala/blob/2.12.x/src/library/scala/concurrent/Promise.scala#L42-L49), [`tryCompleteWith`](https://github.com/scala/scala/blob/2.12.x/src/library/scala/concurrent/Promise.scala#L65-L74), [`success`](https://github.com/scala/scala/blob/2.12.x/src/library/scala/concurrent/Promise.scala#L76-L82), [`trySuccess`](https://github.com/scala/scala/blob/2.12.x/src/library/scala/concurrent/Promise.scala#L84-L90), [`failure`](https://github.com/scala/scala/blob/2.12.x/src/library/scala/concurrent/Promise.scala#L92-L100), [`tryFailure`](https://github.com/scala/scala/blob/2.12.x/src/library/scala/concurrent/Promise.scala#L102-L108) |

Además `Promise` tiene un miembro [`future`](https://github.com/scala/scala/blob/2.12.x/src/library/scala/concurrent/Promise.scala#L29-L31) abstracto y otro [`isCompleted`](https://github.com/scala/scala/blob/2.12.x/src/library/scala/concurrent/Promise.scala#L33-L40).

Una vez entendidos a grandes rasgos estos `trait`s podemos ver como son implementados los métodos abstractos.

En [este](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/impl/Promise.scala) archivo se encuentra la mayoría de la implementación.

## `Promise` (implementación)

Por otro lado el archivo de implementación define un tipo, también llamado `Promise` que debe implementar `Promise` y `Future` del API público, lo que lo hace muy confuso. Implementa algunos de los métodos y deja otros sin implementar. En particular implementa `transform` y `transformWith`. En este punto creo que uno puede entender parcialmente como están implementados: ambos dependen del método `onComplete` que tiene esta firma:

```scala
def onComplete[U](func: Try[T] => U)(implicit executor: ExecutionContext): Unit
```

Sin ponernos a ver como está implementado todavía lo que hace es agregar un callback al futuro para que se ejecute una vez el futuro/promesa se resuelva. Además ambos métodos definen un valor de tipo `DefaultPromise`, que es el que implementa la mayoría de cosas. Ambos métodos lo que hacen es algo como lo siguiente: definir una nueva promesa (el valor de escritura), después al futuro/promesa actual agregarle un callback para que cuando se complete utilizar ese valor de cierta manera para escribir la promesa con el valor, y finalmente devolver el lado de escritura de la promesa, es decir el futuro. La promesa se escribe con el método `tryComplete`, o con alguno de sus derivados.

## `CallbackRunnable`

[`CallbackRunnable`](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/impl/Promise.scala#L54). Es un `Runnable` y por lo tanto implementa un método `run` y puede ser enviado a un `ExecutionContext`. Además tiene una referencia a un `ExecutionContext`. Lo importante es entender que simplemente se trata de una función que hace algo con un valor de tipo `Try[T]` y que puede ejecutarse en un `ExecutionContext`. Aquí lo importante es ver que haría cuando se llame el método `executeWithValue`.
