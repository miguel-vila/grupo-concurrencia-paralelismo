package futuro

import scala.util.Try

sealed trait EstadoPromesa[T]
case class Resuelta[T](value: Try[T]) extends EstadoPromesa[T]
case class Pendiente[T](callbacks: List[Callback[T]]) extends EstadoPromesa[T]
object Pendiente {
  def apply[T](): EstadoPromesa[T] = Pendiente(List.empty)
}