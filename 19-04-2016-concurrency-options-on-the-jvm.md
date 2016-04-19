# [Jessica Kerr's "Concurrency options on the JVM"](https://www.youtube.com/watch?v=yhguOt863nw)

* Threads: One extra flow of execution
  * Useful when waiting for IO or a network request

* keep alive time
* daemon threads


* [`ExecutorService`](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ExecutorService.html) :
  * interface around a thread pool of sorts
  * Extends [`Executor`](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/Executor.html) class with shutdown facilities. `Executor` is just an interface with the method `void	execute(Runnable command)`
  * `void shutdown()`: Initiates an orderly shutdown in which previously submitted tasks are executed, but no new tasks will be accepted. Invocation has no additional effect if already shut down.
  * `boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException`: Blocks until all tasks have completed execution after a shutdown request, or the timeout occurs, or the current thread is interrupted, whichever happens first. Returns true if this executor terminated and false if the timeout elapsed before termination.

* `Executors` class: has static methods to construct Executors

# Actors

# Agents

# Channels

# Parallel streams

# Futures

In Java 5: [`Future`](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/Future.html) to get the value you must block the calling thread. In other `Future` implementations you can attach instructions to the future so you don't have to block. `ExecutorService` has a `Future<?>	submit(Runnable task)` method but the Future doesn't have functional combinator methods (`map`, `flatMap`, etc... what Jessica calls _monadic_ interface)

In Java 8: [`CompletableFuture`](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html): allows to attach instructions for when the future completes. For example: `<U> CompletableFuture<U>	thenApply(Function<? super T,? extends U> fn)`

Default pool in Java 8: [`ForkJoinPool.commonPool()`](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ForkJoinPool.html#commonPool--)

In Scala:

* Scala's [`ExecutionContext`](http://www.scala-lang.org/api/2.11.8/#scala.concurrent.ExecutionContext) is just a wrapper over `Executor`.
* [Global execution context](http://www.scala-lang.org/api/2.11.8/#scala.concurrent.ExecutionContext$): The default ExecutionContext implementation is backed by a work-stealing thread pool. By default, the thread pool uses a target number of worker threads equal to the number of available processors.

# [Executors](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/Executors.html)

## [SingleThreadExecutor](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/Executors.html#newSingleThreadExecutor())

* unlimited queue
* explicit shutdown

> Executor that uses a single worker thread operating off an unbounded queue. (Note however that if this single thread terminates due to a failure during execution prior to shutdown, a new one will take its place if needed to execute subsequent tasks.) Tasks are guaranteed to execute sequentially, and no more than one task will be active at any given time.

* Ideal for swing's event dispatch thread
* Useful for liveness
* For a single owner of mutable state. For example the UI
* Do not block when using it

## [CachedThreadPool](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/Executors.html#newCachedThreadPool())

> a thread pool that creates new threads as needed, but will reuse previously constructed threads when they are available. These pools will typically improve the performance of programs that execute many short-lived asynchronous tasks. Calls to execute will reuse previously constructed threads if available. If no existing thread is available, a new thread will be created and added to the pool. Threads that have not been used for sixty seconds are terminated and removed from the cache. Thus, a pool that remains idle for long enough will not consume any resources.

* synchronized blocking pool
* no queuing
* blocking is ok

## [FixedThreadPool](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/Executors.html#newFixedThreadPool(int))

> Creates a thread pool that reuses a fixed number of threads operating off a shared unbounded queue. At any point, at most nThreads threads will be active processing tasks. If additional tasks are submitted when all threads are active, they will wait in the queue until a thread is available. If any thread terminates due to a failure during execution prior to shutdown, a new one will take its place if needed to execute subsequent tasks. The threads in the pool will exist until it is explicitly shutdown.

* For throttling, e.g. a service
* Don't do recursive blocking (possibility of a pool-induced deadlock)
* Watch out for other people's code (when passing it to other code?)

## [ThreadPoolExecutor](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ThreadPoolExecutor.html)

* core pool size
* max pool size
* queue
  * 0: SynchronousQueue
  * inf: LinkedBlockingQueue
  * ...
* ThreadFactory
  * daemon?
  * name
  * priority
  * ...
* RejectedExecutionHandler
  * Default throw `RejectedExecutionException`
  Options:
  * caller Returns
  * Discard task
  * Discard old task: e.g. the oldest click in the UI

## [ForkJoinPool](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ForkJoinPool.html)

* Maximize processor power
* unbounded queues
* daemon threads

In Scala:

[`blocking`](http://docs.scala-lang.org/overviews/core/futures.html) construct:
>  it is possible to notify an ExecutionContext of a blocking call with the blocking construct.

```scala
  blocking {
    someCode
  }
```


# When using a Thread pool

Whose threads am I using?

1. What is done?
2. How many is the right many?
3. What happens in failure?

# Algunas lecciones

* Don't share mutable state among multiple threads
* Never block forever! Always provide a timeout
* Avoid Pool-induced deadlock when using fixed size pool threads
* Calling `shutdown` on a `ForkJoinPool` won't do anything because it's fully intended to accept tasks that submit other tasks back into the same pool (Computations that start other computations recursively). All you can do is wait until the thread pool is doing nothing and exit before anyone externally submits a task into the pool. (`fjp.awaitQuiescence(5,MINUTES)`)

# Dudas por resolver
