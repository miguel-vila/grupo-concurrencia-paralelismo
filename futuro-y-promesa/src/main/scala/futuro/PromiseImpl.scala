package futuro

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.Try

class PromiseImpl[T](state: AtomicReference[PromiseState[T]]) extends Promesa[T] with Futuro[T] {

  override def futuro: Futuro[T] = this

  def onComplete(f: Try[T] => Unit)(implicit executionContext: ExecutionContext): Unit ={
    val callback = new Callback[T](f,executionContext)
    onComplete(callback)
  }

  def onComplete(callback: Callback[T]): Unit = {
    state.get() match {
      case Resolved(value)           =>
        callback.executeWith(value)
      case currentState @ Pending(currentCallbacks) =>
        if(state.compareAndSet(currentState, Pending( callback :: currentCallbacks)))
          ()
        else
          onComplete(callback)
    }
  }

  def complete(value: Try[T]): Boolean = {
    getCallbacksAndSetValue(value) match {
      case None => false
      case Some(listeners) =>
        listeners.foreach(_.executeWith(value))
        true
    }
  }

  @tailrec
  private final def getCallbacksAndSetValue(value: Try[T]): Option[List[Callback[T]]] = {
    val currentState = state.get()
    currentState match {
      case Resolved(_) => None
      case Pending(currentCallbacks) =>
        if (state.compareAndSet(currentState, Resolved(value))) {
          Some(currentCallbacks)
        } else {
          getCallbacksAndSetValue(value)
        }
    }
  }

  def waitForResult(timeout: FiniteDuration)(implicit executionContext: ExecutionContext): Try[T] = {
    state.get() match {
      case Resolved(value) => value
      case _               =>
        var resultRef: Option[Try[T]] = None
        val latch = new CountDownLatch(1)
        onComplete { result =>
          resultRef = Some(result)
          latch.countDown()
        }
        latch.await(timeout.length, timeout.unit)
        resultRef.get
    }
  }

}