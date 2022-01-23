package pers.rdara.akka.http.test.server.services

import akka.http.scaladsl.model.Uri.Path.SingleSlash
import akka.http.scaladsl.model.{HttpCharsets, HttpEntity, HttpRequest, MediaType, StatusCodes}
import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directives
import akka.util.ByteString
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import io.prometheus.client.hotspot.DefaultExports
import pers.rdara.akka.http.test.server.common.ApplicationConfig

import java.io.{StringWriter, Writer}
import scala.concurrent.ExecutionContext

/**
  * @author Ramesh Dara
 */

object Metrics extends Directives {

  private val prometheusTextType =
    MediaType.customWithFixedCharset("text", "plain", HttpCharsets.`UTF-8`, params = Map("version" → "0.0.4"))

    DefaultExports.initialize()

  def metricsRoute(implicit ec: ExecutionContext): server.Route = ignoreTrailingSlash {
    (get & path("metrics")){
      complete {
        val writer: Writer = new StringWriter()
        TextFormat.write004(writer, CollectorRegistry.defaultRegistry.metricFamilySamples())
        StatusCodes.OK -> HttpEntity(prometheusTextType, ByteString(writer.toString))
      }
    }
  }

  def getMetricKeys(request: HttpRequest): Seq[String] = {
    val depth: Integer = 3
    ApplicationConfig.Default.metrics.default_keys ++
      Seq(request.method.value) ++
      request.getUri().asScala().path.toString().split(SingleSlash.toString()).filter(_.nonEmpty).take(depth)
  }

  def getLabelledMetricKeys(): Seq[String] = ApplicationConfig.Default.metrics.labelled_keys

  def getMetricLables(request: HttpRequest): Seq[String] = {
    val labels = Seq(request.method.value) ++
      request.getUri().asScala().path.toString().split(SingleSlash.toString()).filter(_.nonEmpty)
    val no_of_lables = ApplicationConfig.Default.metrics.no_of_labels
    labels.padTo(no_of_lables,"").take(no_of_lables)
  }

}

