package futuro

import java.util.concurrent.atomic.AtomicReference

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success, Try}

trait Promesa[T] {

  def futuro: Futuro[T]

  def complete(value: Try[T]): Boolean

}

object Promesa {

  def apply[T](): Promesa[T] = new PromiseImpl[T](state = new AtomicReference(Pending()))

  private def fullfilledPromise[T](value: Try[T]): Promesa[T] = new Promesa[T] {

    override def futuro: Futuro[T] = new Futuro[T] {
      override def onComplete(f: Try[T] => Unit)(implicit executionContext: ExecutionContext): Unit = {
        val callback = new Callback[T](f, executionContext)
        callback.executeWith(value)
      }

      override def waitForResult(timeout: FiniteDuration)(implicit executionContext: ExecutionContext): Try[T] = value
    }

    override def complete(value: Try[T]): Boolean = false
  }

  def successful[T](value: T): Promesa[T] = fullfilledPromise(Success(value))

  def failed[T](error: Throwable): Promesa[T] = fullfilledPromise(Failure(error))

}