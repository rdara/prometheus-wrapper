package pers.rdara.akka.http.test.server

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{RejectionHandler, Route}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import pers.rdara.akka.http.test.server.common.{ApplicationConfig, ApplicationContext, CommonExceptionHandler, PrometheusMetricsDirectives}
import pers.rdara.akka.http.test.server.common.Jackson.AkkaHttpSupport
import pers.rdara.akka.http.test.server.services.{DemoService, MetricsService}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
 * @author Ramesh Dara
*/

class TestServer(appContext: ApplicationContext) extends CommonExceptionHandler(appContext) with PrometheusMetricsDirectives with LazyLogging {
  import appContext.Implicits._
  implicit val timeout: Timeout = Timeout(10.seconds)


  def startServer(bind: String, port: Int, appConfig: ApplicationConfig)(implicit materializer: ActorMaterializer): Future[Http.ServerBinding] = {
    import akka.http.scaladsl.server.Directives._

    val services: Route = List(
      new MetricsService(appContext),
      new DemoService(appContext)
    ).map(_.routes).reduceLeft(_ ~ _)

    val rejectionHandler = AkkaHttpSupport.rejectionHandler withFallback RejectionHandler.default

    val aggregatedRoutes = injectStartTimeHeader(materializer) {
        handleRejections(rejectionHandler) {
          handleExceptions(exceptionHandler) {
            initiatePrometheusMetrics(materializer) {
            completePrometheusMetrics(materializer) {
              services
            }
          }
        }
      }
    }

    val bindingFuture = appContext.http.bindAndHandle(aggregatedRoutes, bind, port)

    bindingFuture.onComplete {
      case Success(binding) ⇒
        logger.info(s"Test Server successfully bound to ${binding.localAddress}")
      case Failure(cause) ⇒
        logger.error(s"Failed to bind test Server at $bind:$port!", cause)
    }
    bindingFuture
  }

  def shutdown: Future[Any] = {
    appContext.shutdown()
  }

  sys.addShutdownHook { () ⇒
    shutdown
  }

}

object TestServer extends App with LazyLogging {
  import scala.concurrent.ExecutionContext.Implicits.global

  val appContext = ApplicationContext()
  val appConfig = appContext.appConfig

  val testServer = new TestServer(appContext)

  val host = appConfig.server.host
  val port = appConfig.server.port

  implicit val materializer = ActorMaterializer()(appContext.Implicits.implicitSystem)
  testServer.startServer(host, port, appConfig).foreach { binding ⇒
    logger.info(s"Test Server is listening at $host:${binding.localAddress.getPort}")
  }

}
