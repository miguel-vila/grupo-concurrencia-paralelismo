# ¿Cómo funcionan los futuros de la librería estándar de Scala?

## Algunos "prerrequisitos"

La idea era que cada material fuera autocontenido, pero para entender cómo están implementados los futuros mínimo toca saber para qué sirven y qué código nos permiten escribir. Creo que [este](http://danielwestheide.com/blog/2013/01/09/the-neophytes-guide-to-scala-part-8-welcome-to-the-future.html) es un buen artículo.

Además para entender la implementación hay que saber algo sobre el tipo `Try`. [Esta](http://danielwestheide.com/blog/2012/12/26/the-neophytes-guide-to-scala-part-6-error-handling-with-try.html) es una buena fuente.

## Material base

Código fuente (la versión más reciente a 19/04/16):

* Paquete `scala.concurrent`
  * [`Future` (`trait` del API público)](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/Future.scala)
  * [`Promise` (`trait` del API público)](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/Promise.scala)
* Paquete `scala.concurrent.impl` (Implementación):
  * [`Promise`](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/impl/Promise.scala)

## Que hay que entender

El código fuente es un poco difícil de recorrer y entender.  Creo que uno entiende casi todo si se entienden los siguientes métodos:

* `map`
* `flatMap`
* `recover`
* `recoverWith`
* `Future.apply` (el método que se llama cuando uno hace `Future {...}`)
* `Future.successful`
* `Future.failed`
* `Future.sequence`
* `Future.traverse`

Primero creo que vale la pena entender el API público. `Future` representa un valor futuro (que puede ser exitoso o fallido con una excepción) y tiene combinadores funcionales como `map` o `flatMap` que permiten crear otros futuros. `Promise` es un objeto que puede ser completado con un valor o fallido con una excepción. La diferencia entre ambos es que `Future` es como el lado de lectura y `Promise` es el lado de escritura, dónde se puede asignar el valor. `Future` es el sitio dónde agregamos funciones que se deberían ejecutar cuando el valor de la promesa haya sido llenado. Además un objeto de tipo `Promise[T]` tiene una referencia a un valor de tipo `Future[T]`, es decir a su lado de escritura. Esto resulta importante a la hora de ver como están implementados ciertos métodos. Creo que si al leer el código uno tiene en cuenta esta relación se puede entender buena parte. Además los comentarios al inicio de cada definición resultan útiles para entender.

## Un camino aproximado para entenderlo

### [`Future`](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/Future.scala#L92) (API público)

`Future` es un `trait` que tiene algunos métodos abstractos y otros implementados en función de los abstractos, entre ellos los que nos interesan son estos:

| Método abstracto | Métodos que usan el método abstracto |
|------------------|--------------------------------------|
| [`transform`](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/Future.scala#L223-L231) | [`map`](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/Future.scala#L244-L264), [`recover`](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/Future.scala#L344-L361) |
| [`transformWith`](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/Future.scala#L233-L241) | [`flatMap`](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/Future.scala#L266-L280), [`recoverWith`](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/Future.scala#L363-L384) |
| [`onComplete`](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/Future.scala#L136-L149) | [`foreach`](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/Future.scala#L195-L205)

Es decir, si entendemos cómo están implementados `transform` y `transformWith` entendemos algunas de la funciones que nos interesan. Las funciones `Future.sequence` y `Future.traverse` usan `map` y `flatMap` y se pueden entender si vemos una versión menos generalizada de ellas que podemos ver más adelante.

Además `Future` tiene un método abstracto [`value`](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/Future.scala#L163-L174) (¿Por qué en los comentarios dice que es no determinístico?) y otro [`isCompleted`](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/Future.scala#L154-L161).

### [`Promise`](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/Promise.scala#L28) (API público)

Por otra parte está `Promise` que tiene métodos para completar la promesa con un valor exitoso (un `Success(t)`) o con una excepción (un `Failure(throwable)`):

| Método abstracto | Métodos que usan el método abstracto |
| ---------------- | ------------------------------------ |
| [`tryComplete`](https://github.com/scala/scala/blob/2.12.x/src/library/scala/concurrent/Promise.scala#L51-L57) | [`complete`](https://github.com/scala/scala/blob/2.12.x/src/library/scala/concurrent/Promise.scala#L42-L49), [`tryCompleteWith`](https://github.com/scala/scala/blob/2.12.x/src/library/scala/concurrent/Promise.scala#L65-L74), [`success`](https://github.com/scala/scala/blob/2.12.x/src/library/scala/concurrent/Promise.scala#L76-L82), [`trySuccess`](https://github.com/scala/scala/blob/2.12.x/src/library/scala/concurrent/Promise.scala#L84-L90), [`failure`](https://github.com/scala/scala/blob/2.12.x/src/library/scala/concurrent/Promise.scala#L92-L100), [`tryFailure`](https://github.com/scala/scala/blob/2.12.x/src/library/scala/concurrent/Promise.scala#L102-L108) |

Además `Promise` tiene un miembro [`future`](https://github.com/scala/scala/blob/2.12.x/src/library/scala/concurrent/Promise.scala#L29-L31) abstracto y otro [`isCompleted`](https://github.com/scala/scala/blob/2.12.x/src/library/scala/concurrent/Promise.scala#L33-L40).

Una vez entendidos a grandes rasgos estos `trait`s podemos ver como son implementados los métodos abstractos.

En [este](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/impl/Promise.scala) archivo se encuentra la mayoría de la implementación.

### [`Promise`](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/impl/Promise.scala#L21) (implementación parcial)

El archivo de implementación define un tipo, también llamado `Promise` que extiende `Promise` y `Future` del API público, lo que lo hace muy confuso. Implementa algunos de los métodos y deja otros sin implementar. En particular implementa `transform` y `transformWith`. En este punto creo que uno puede entender parcialmente como están implementados: ambos dependen del método `onComplete` que tiene esta firma:

```scala
def onComplete[U](func: Try[T] => U)(implicit executor: ExecutionContext): Unit
```

Sin ponernos a ver como está implementado todavía lo que `onComplete` hace es agregar una función al futuro para que se ejecute una vez el futuro/promesa se resuelva. Además ambos métodos (`transform` y `transformWith`) definen un valor de tipo `DefaultPromise`, que es el que implementa la mayoría de cosas. Ambos métodos lo que hacen es algo como lo siguiente: definir una nueva promesa (el valor de escritura), después al futuro/promesa actual agregarle un callback para que cuando se complete utilizar ese valor de cierta manera para escribir la promesa con el valor, y finalmente devolver el lado de escritura de la promesa, es decir el futuro. La promesa se escribe con el método `tryComplete`, o con alguno de sus derivados. Ambos métodos siguen un esquema como el siguiente:

```scala
val p = new DefaultPromise[S]()  // Definir lado de escritura
onComplete { result: T =>
  /*
   * a partir de algo de tipo T transformarlo en algo de tipo S
   * y escribir ese valor en la promesa usando alguno de complete
   * , failure o completeWith
   */  
}
p.future // retornar lado de lectura
```

### [`DefaultPromise`](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/impl/Promise.scala#L183) (mayoría de la implementación)

`DefaultPromise` hereda de `AtomicReference` e implementa `Promise` (el que se encuentra en el mismo archivo, es decir la implementación parcial). [`AtomicReference`](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/atomic/AtomicReference.html) es una clase del API de concurrencia de Java y sirve para mantener una referencia mutable que se puede actualizar atómicamente, es decir se puede compartir entre distintos _threads_ sin riesgo de "corromper" el valor. Los métodos que deberían entender son `get` y `compareAndSet`. Los [comentarios](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/impl/Promise.scala#L105-L127) de `DefaultPromise` creo que explican gran parte de la cosa:

```scala
/** Default promise implementation.
 *
 *  A DefaultPromise has three possible states. It can be:
 *
 *  1. Incomplete, with an associated list of callbacks waiting on completion.
 *  2. Complete, with a result.
 *  3. Linked to another DefaultPromise.
 *
 *  If a DefaultPromise is linked to another DefaultPromise, it will
 *  delegate all its operations to that other promise. This means that two
 *  DefaultPromises that are linked will appear, to external callers, to have
 *  exactly the same state and behaviour. For instance, both will appear as
 *  incomplete, or as complete with the same result value.
 *
 *  A DefaultPromise stores its state entirely in the AnyRef cell exposed by
 *  AtomicReference. The type of object stored in the cell fully describes the
 *  current state of the promise.
 *
 *  1. List[CallbackRunnable] - The promise is incomplete and has zero or more callbacks
 *     to call when it is eventually completed.
 *  2. Try[T] - The promise is complete and now contains its value.
 *  3. DefaultPromise[T] - The promise is linked to another promise.
 *
 * The ability to link DefaultPromises is needed to prevent memory leaks when
 * using Future.flatMap...
**/
```

Los puntos 1 y 2 deberían ser familiares. Pero el 3ro es una optimización hacen para prevenir _memory leaks_, ese es un detalle de la implementación que no he entendido bien, pero igual se puede entender lo demás sin eso.

Los métodos más importantes de `DefaultPromise` que deben entender son:

* `onComplete`
* `dispatchOrAddCallback` que es el que se usa para implementar `onComplete`
* `tryComplete`

Para `dispatchOrAddCallback` se usa un objeto del siguiente tipo:

### [`CallbackRunnable`](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/impl/Promise.scala#L54)

Es un `Runnable` y por lo tanto implementa un método `run` y puede ser enviado a un `ExecutionContext`. Además tiene una referencia a un `ExecutionContext`. Lo importante es entender que simplemente se trata de una función que hace algo con un valor de tipo `Try[T]` y que puede ejecutarse en un `ExecutionContext`.

### [`Kept`](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/impl/Promise.scala#L358)

Este `trait` sirve para describir una una promesa que ha sido inmediatamente completada con algún valor y sirve para implementar [`Promise.Successful`](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/impl/Promise.scala#L375) y [`Promise.Failed`](https://github.com/scala/scala/blob/804a4cc1ff9fa159c576be7c685dbb81220c11da/src/library/scala/concurrent/impl/Promise.scala#L383) que a su vez son usados para implementar `Future.successful` y `Future.failed` respectivamente.

---

Esto creo que es la mayoría de lo que hay entender. Faltan varias cosas pero durante la sesión podemos adentrarnos en los detalles de cada método y en las preguntas específicas de concurrencia.
