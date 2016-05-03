package futuro

import scala.util.Try

sealed trait PromiseState[T]
case class Resolved[T](value: Try[T]) extends PromiseState[T]
case class Pending[T](callbacks: List[Callback[T]]) extends PromiseState[T]
object Pending {
  def apply[T](): Pending[T] = Pending(List.empty)
}