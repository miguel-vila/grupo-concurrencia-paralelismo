package futuro

import scala.concurrent.ExecutionContext
import scala.util.Try
import scala.util.control.NonFatal

class Callback[T](callback: Try[T] => Unit, executionContext: ExecutionContext) {

  def executeWith(value: Try[T]): Unit = executionContext.execute(new Runnable {
    override def run(): Unit = {
      try {
        callback(value)
      } catch {
        case NonFatal(t) => executionContext.reportFailure(t)
      }
    }
  })

}