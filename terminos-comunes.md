# Términos Comúnes

## Thread safety

> (...) a class is thread-safe when it continues to behave correctly when accessed from multiple threads.

(Java Concurrency in Practice, Chapter 2, pg 17)

## Race condition

> A race condition occurs when the correctness of a computation depends on the relative timing or interleaving of multiple threads by the runtime; in other words, when getting the right answer relies on lucky timing.

(Java Concurrency in Practice, Chapter 2, pg 20)

## _check-then-act_

> The most common type of race condition is check-then-act, where a potentially stale observation is used to make a decision on what to do next.

(Java Concurrency in Practice, Chapter 2, pg 20)

> (...) This type of race condition is called check-then-act: you observe something to be true (file X doesn’t exist) and then take action based on that observation (create X); but in fact the observation could have become invalid between the time you observed it and the time you acted on it (someone else created X in the meantime), causing a problem (unexpected exception, overwritten data, file corruption).

(Java Concurrency in Practice, Chapter 2, pg 21)

## Intrinsic locks (Java)

> Every Java object can implicitly act as a lock for purposes of synchronization; these built-in locks are called intrinsic locks or monitor locks. The lock is automatically acquired by the executing thread before entering a synchronized block and automatically released when control exits the synchronized block, whether by the normal control path or by throwing an exception out of the block. The only way to acquire an intrinsic lock is to enter a synchronized block or method guarded by that lock.

> (...) Since only one thread at a time can execute a block of code guarded by a given lock, the synchronized blocks guarded by the same lock execute atomically with respect to one another.

(Java Concurrency in Practice, Chapter 2, pg 25)

## Reentrancy

> (...) intrinsic locks are reentrant, if a thread tries to acquire a lock that it already holds, the request succeeds. Reentrancy means that locks are acquired on a per-thread rather than per-invocation basis.

(Java Concurrency in Practice, Chapter 2, pg 27)

> When a thread acquires a previously unheld lock, the JVM records the owner and sets the acquisition count to one. If that same thread acquires the lock again, the count is incremented, and when the owning thread exits the synchronized block, the count is decremented. When the count reaches zero, the lock is released.

Reentrancy saves us from deadlock in situations like this:

```java
public class Widget {
  public synchronized void doSomething() {
    ...
  }
}

public class LoggingWidget extends Widget {
  public synchronized void doSomething() {
    System.out.println(toString() + ": calling doSomething");
    super.doSomething();
  }
}
```

(Java Concurrency in Practice, Chapter 2, pg 27,28)
