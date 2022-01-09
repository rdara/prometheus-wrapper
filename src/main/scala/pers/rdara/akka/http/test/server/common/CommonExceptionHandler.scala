package pers.rdara.akka.http.test.server.common

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives.{complete, extractRequest}
import akka.http.scaladsl.server.{Directive1, Directives, ExceptionHandler}
import com.typesafe.scalalogging.LazyLogging
import Utilities.getDuration
import pers.rdara.akka.http.test.server.model.GenericErrorResponse
import pers.rdara.akka.http.test.server.services.Metrics
import pers.rdara.prometheus.wrapper.metrics.{DefaultMetrics, LabelledMetrics}

import javax.security.sasl.AuthenticationException

/**
 * @author Ramesh Dara
*/

abstract class CommonExceptionHandler(appContext: ApplicationContext) extends Jackson.AkkaHttpSupport with LazyLogging {

  private def extractErrorRequest(e: Throwable): Directive1[(HttpRequest)] = {
    extractRequest.flatMap { request ⇒
      LabelledMetrics.erroredMessage(getDuration(request), Metrics.getMetricLables(request))
      DefaultMetrics.erroredMessage(getDuration(request), Metrics.getMetricKeys(request))
      Directives.provide(request)
    }
  }

  implicit def exceptionHandler: ExceptionHandler = {
    ExceptionHandler {
      case e: AuthenticationException ⇒
        extractErrorRequest(e) {
          case (req) ⇒
            logger.warn(s"Authentication failed", e)
            complete(Unauthorized → GenericErrorResponse("Authentication error"))
        }
      case e: Throwable ⇒
        extractErrorRequest(e) {
          case (req) ⇒
            logger.error(s"Unhandled error found for the request: ${req.uri}, reason: ${e.getMessage}", e)
            complete(InternalServerError → GenericErrorResponse(e))
        }
    }
  }
}
