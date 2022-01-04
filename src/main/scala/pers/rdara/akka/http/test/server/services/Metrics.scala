package pers.rdara.akka.http.test.server.services

import akka.http.scaladsl.model.Uri.Path.SingleSlash
import akka.http.scaladsl.model.{HttpCharsets, HttpEntity, HttpRequest, MediaType, StatusCodes}
import akka.http.scaladsl.server
import akka.http.scaladsl.server.Directives
import akka.util.ByteString
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat
import io.prometheus.client.hotspot.DefaultExports

import java.io.{StringWriter, Writer}
import scala.concurrent.ExecutionContext

/**
  * @author Ramesh Dara
 */
trait Metrics {
  def startMessage(keys: String*)
  def completedMessage(processingTime: Long, keys: String*): Unit
  def erroredMessage(processingTime: Long, keys: String*): Unit

  def getActiveMessagesCount: Int
  def getTotalMessagesCount: Int
}

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

  def getMetricKeys(request: HttpRequest) = {
    Seq("prometheus", "wrapper", request.method.value) ++
      request.getUri().asScala().path.toString().split(SingleSlash.toString()).filter(_.nonEmpty)
  }

}

