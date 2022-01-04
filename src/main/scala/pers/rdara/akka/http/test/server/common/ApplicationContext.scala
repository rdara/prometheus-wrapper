package pers.rdara.akka.http.test.server.common

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.{ExecutionContext, Future}

/**
 * @author Ramesh Dara
 */

/**
  * The trait def functions can be overridden as vals to eliminate uninitialized vals.
  * The class properties must be defined as `lazy val` because they can be overridden by other vals without
  * having an undesired effect (uninitialized values).
  */
trait ApplicationContext {
  def appConfig: ApplicationConfig
  def system: ActorSystem
  def materializer: ActorMaterializer
  def ec: ExecutionContext
  def http: HttpExt

  def shutdown()(implicit ec: ExecutionContext): Future[Any] = {
    for {
      _ ← http.shutdownAllConnectionPools()
      _ ← system.terminate()
    } yield ()
  }

  object Implicits {
    implicit val implicitEc: ExecutionContext = ec
    implicit val implicitSystem: ActorSystem = system
    implicit val implicitMaterializer: Materializer = materializer
  }
}

class DefaultApplicationContext extends ApplicationContext with LazyLogging {
  override val appConfig: ApplicationConfig = ApplicationConfig.Default
  override implicit lazy val system: ActorSystem = ActorSystem("prometheus-wrapper-demo", appConfig.underlying)
  override implicit lazy val materializer: ActorMaterializer = ActorMaterializer()(system)
  override implicit lazy val ec: ExecutionContext = system.dispatchers.lookup("akka.http.test.blocking-io-dispatcher")
  override lazy val http: HttpExt = Http()(system)
}

//Application context with all the required implicits like ExecutionContext
object ApplicationContext {
  lazy val Default: ApplicationContext = new DefaultApplicationContext
  def apply(): ApplicationContext = Default
}
