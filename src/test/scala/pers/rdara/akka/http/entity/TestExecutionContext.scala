package pers.rdara.akka.http.entity

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.{ActorMaterializer, Materializer}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Milliseconds, Minutes, Seconds, Span}
import pers.rdara.akka.http.test.server.common.ApplicationContext.Default.appConfig

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.{Duration, FiniteDuration}


trait TestExecutionContext extends ScalaFutures {
  implicit val system: ActorSystem        = ActorSystem("EntitiesTests", appConfig.underlying)
  implicit val materializer: Materializer = ActorMaterializer()(system)

  implicit val defaultPatience: PatienceConfig = PatienceConfig(timeout = Span(10, Minutes), interval = Span(25, Milliseconds))

  val http: HttpExt = Http()(system)

  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global

  implicit class AwaitFuture[T](future: Future[T]) {
    def await(duration: FiniteDuration): T = {
      Await.result(future, duration)
    }
    def awaitIndefinitely(): T = {
      Await.result(future, Duration.Inf)
    }
  }
}