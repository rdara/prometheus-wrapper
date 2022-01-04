package pers.rdara.akka.http.test.server.services

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import pers.rdara.akka.http.test.server.common.Utilities.getBaseUrl
import pers.rdara.akka.http.test.server.common.{ApplicationContext, Jackson}
import pers.rdara.akka.http.test.server.model.ServiceRoutes

/**
  * @author Ramesh Dara
  * @since Jun-2019
  */
class DemoService(appContext: ApplicationContext) extends ServiceRoutes with Directives with Jackson.AkkaHttpSupport {
  case class message(msg: String)
  val metricKeyForMessages = Seq("prometheus", "metrics", "demo")
  override val routes: Route = {
    ignoreTrailingSlash {
      pathPrefix("demo") {
        (get & path("short")) {
          complete {
            val timeStamp = System.currentTimeMillis
            // If you want to define application-specific prefix keys, above is equivalent to
            // Above is also equivalent to:
            //DefaultMetrics.startMessage(metricKeyForMessages :+ "short": _*)
            StatusCodes.OK -> message(s"Try '${getBaseUrl(appContext)}/metrics' to see the metrics starting with prometheus_wrapper_get_demo_short_")
          }
        } ~
        (get & path("long")) {
          complete {
            Thread.sleep(10000) //Wait for 10 seconds
            StatusCodes.OK -> message(s"Try '${getBaseUrl(appContext)}/metrics' to see the metrics starting with prometheus_wrapper_get_demo_long_")
          }
        } ~
        (get & path("error")) {
          complete {
            throw new RuntimeException(s"Try '${getBaseUrl(appContext)}/metrics' to see the metrics starting with prometheus_wrapper_get_demo_error_")
          }
        }
      }
    }
  }
}
