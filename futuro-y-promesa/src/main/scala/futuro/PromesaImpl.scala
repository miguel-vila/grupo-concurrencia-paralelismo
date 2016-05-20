package futuro

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.Try

class PromesaImpl[T] extends AtomicReference[EstadoPromesa[T]](Pendiente()) with Promesa[T] with Futuro[T] {

  override def futuro: Futuro[T] = this

  def onComplete(f: Try[T] => Unit)(implicit executionContext: ExecutionContext): Unit ={
    val callback = new Callback(f,executionContext)
    onComplete(callback)
  }

  @tailrec
  private def onComplete(callback: Callback[T]): Unit = {
    get() match {
      case Resuelta(value)           =>
        callback.executeWith(value)
      case currentState @ Pendiente(currentCallbacks) =>
        if(compareAndSet(currentState, Pendiente( callback :: currentCallbacks)))
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
  private def getCallbacksAndSetValue(value: Try[T]): Option[List[Callback[T]]] = {
    get() match {
      case Resuelta(_) =>
        None
      case currentState @ Pendiente(currentCallbacks) =>
        if (compareAndSet(currentState, Resuelta(value))) {
          Some(currentCallbacks)
        } else {
          getCallbacksAndSetValue(value)
        }
    }
  }

  def waitForResult(timeout: FiniteDuration)(implicit executionContext: ExecutionContext): Try[T] = {
    get() match {
      case Resuelta(value) => value
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