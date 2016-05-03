package futuro

import java.util.concurrent.{ExecutorService, Executors, TimeUnit}

import org.scalatest._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.util.Success

/**
  * Created by miguel on 4/30/16.
  */
class FuturoTestsCathedThreadPool extends FuturoTestsWithExecutor( () => Executors.newCachedThreadPool() )

// class FuturoTestsSingleThreadPool extends FuturoTestsWithExecutor( () => Executors.newSingleThreadExecutor() )

class FuturoTestsWithExecutor(createExecutor: () => ExecutorService) extends FunSuite with BeforeAndAfterEach {

  var executor: ExecutorService = _
  implicit var executionContext: ExecutionContext = _

  override def beforeEach() = {
    executor = createExecutor()
    executionContext = ExecutionContext.fromExecutorService(executor)
  }

  def computeFuture(x: Int)(sleep: FiniteDuration): Futuro[Int] = {
    val promesa = Promesa[Int]()
    executionContext.execute(new Runnable {
      override def run(): Unit = {
        Thread.sleep(sleep.toMillis)
        promesa.complete(Success(2 * x))
      }
    })
    promesa.futuro
  }

  test("simple 1: map with thread.sleep") {
    val promesa = Promesa[Int]()
    executionContext.execute(new Runnable {
      override def run(): Unit = {
        Thread.sleep(100)
        promesa.complete(Success(2))
      }
    })
    val futuro = promesa.futuro.map { 3 * _ }
    val value = futuro.waitForResult(200 millis)
    assert( value == Success(6) )
    executor.awaitTermination(200, TimeUnit.MILLISECONDS)
  }

  test("simple 2: flatmap with thread.sleep") {
    val promesa = Promesa[Int]()
    executionContext.execute(new Runnable {
      override def run(): Unit = {
        Thread.sleep(100)
        promesa.complete(Success(7))
      }
    })
    val futuro = promesa.futuro.flatMap(x => computeFuture(x)(100 millis))
    val value = futuro.waitForResult(225 millis)
    assert( value == Success(14) )
    executor.awaitTermination(200, TimeUnit.MILLISECONDS)
  }

  test("simple 3: parallel futures join") {
    val f1 = computeFuture(2)(100 millis)
    val f2 = computeFuture(5)(100 millis)

    val futuro = for {
      x1 <- f1
      x2 <- f2
    } yield x1 + x2

    val result = futuro.waitForResult(150 millis)
    assert(result == Success(14))

    executor.awaitTermination(200, TimeUnit.MILLISECONDS)
  }

}
