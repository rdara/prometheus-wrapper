package pers.rdara.akka.http.test.server.common

import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives.{mapInnerRoute, mapRequest}
import akka.http.scaladsl.server.RouteResult.{Complete, Rejected}
import akka.stream.Materializer
import pers.rdara.akka.http.test.server.TestServer
import Utilities.{getDuration, startTimeHeaderName}
import pers.rdara.akka.http.test.server.services.Metrics
import pers.rdara.prometheus.wrapper.metrics.{DefaultMetrics, LabelledMetrics}

import scala.concurrent.ExecutionContext

/**
 * @author Ramesh Dara
 * @since Jan-2022
 */
trait PrometheusMetricsDirectives {
  this: TestServer =>

  def injectStartTimeHeader(implicit materializer: Materializer): Directive0 = {
    mapRequest(request => {
      request.addHeader(RawHeader(startTimeHeaderName, System.currentTimeMillis.toString))
    })
  }

  def initiatePrometheusMetrics(implicit materializer: Materializer): Directive0 = {
    mapRequest(request => {
      LabelledMetrics.startMessage(Metrics.getMetricLables(request))
      DefaultMetrics.startMessage(Metrics.getMetricKeys(request))
      request
    })
  }

  def completePrometheusMetrics(implicit materializer: Materializer): Directive0 = {
    implicit val ec: ExecutionContext = materializer.executionContext
    mapInnerRoute(route ⇒ ctx =>
      route(ctx) map {
        case Complete(response) ⇒
          LabelledMetrics.completedMessage(getDuration(ctx.request), Metrics.getMetricLables(ctx.request))
          DefaultMetrics.completedMessage(getDuration(ctx.request), Metrics.getMetricKeys(ctx.request))
          Complete(response)
        case routeResult@Rejected(rejections) ⇒
          routeResult
      })
  }
}
