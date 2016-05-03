package futuro

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

trait Futuro[T] {

  def onComplete(f: Try[T] => Unit)(implicit executionContext: ExecutionContext): Unit

  def map[S](f: T => S)(implicit executionContext: ExecutionContext): Futuro[S] = {
    val promise = Promesa[S]()
    onComplete {
      case Success(t) =>
        val value = try { Success( f(t) ) } catch { case NonFatal(error) => Failure(error) }
        promise.complete( value )
      case Failure(error) =>
        promise.complete( Failure(error) )
    }
    promise.futuro
  }

  def recover[S>:T](handle: PartialFunction[Throwable, S])(implicit executionContext: ExecutionContext): Futuro[S] = {
    val promise = Promesa[S]()
    onComplete {
      case Success(t) =>
        promise.complete(Success(t))
      case Failure(error) =>
        if(handle.isDefinedAt(error)) {
          val value = try { Success( handle.apply(error) ) } catch { case NonFatal(error) => Failure(error) }
          promise.complete(value)
        } else {
          promise.complete( Failure(error) )
        }
    }
    promise.futuro
  }

  def flatMap[S](f: T => Futuro[S])(implicit executionContext: ExecutionContext): Futuro[S] = {
    val promise = Promesa[S]()
    onComplete {
      case Success(t) =>
        try {
          f(t).onComplete { result2 => promise.complete( result2 ) }
        } catch {
          case NonFatal(error) => Failure(error)
        }
      case Failure(error) =>
        promise.complete( Failure(error) )
    }
    promise.futuro
  }

  def recoverWith[S>:T](handle: PartialFunction[Throwable, Futuro[S]])(implicit executionContext: ExecutionContext): Futuro[S] = {
    val promise = Promesa[S]()
    onComplete {
      case Success(t) =>
        promise.complete(Success(t))
      case Failure(error) =>
        if(handle.isDefinedAt(error)) {
          try {
            handle.apply(error).onComplete { result2 =>  promise.complete(result2) }
          } catch {
            case NonFatal(error) => promise.complete( Failure(error) )
          }
        } else {
          promise.complete( Failure(error) )
        }
    }
    promise.futuro
  }

  def waitForResult(timeout: FiniteDuration)(implicit executionContext: ExecutionContext): Try[T]

}

object Futuro {

  def unit(): Futuro[Unit] = successful( () )

  def apply[T](body : => T)(implicit executionContext: ExecutionContext): Futuro[T] = unit().map(_ => body)

  def successful[T](value: T): Futuro[T] = Promesa.successful(value).futuro

  def failed[T](error: Throwable): Futuro[T] = Promesa.failed(error).futuro

/*
  def sequence[T](futures: List[Futuro[T]]): Futuro[List[T]] = {
    ???
  }
  */

}