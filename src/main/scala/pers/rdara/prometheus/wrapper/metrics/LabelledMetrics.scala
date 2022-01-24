package pers.rdara.prometheus.wrapper.metrics

import com.typesafe.scalalogging.LazyLogging
import pers.rdara.akka.http.test.server.common.ApplicationConfig
import pers.rdara.prometheus.wrapper.metrics.interfaces.MetricsInterface

/**
 * @author Ramesh Dara
 * @since Jun-2019
 */
object LabelledMetrics extends AbstractMetrics with MetricsInterface with LazyLogging {
  lazy val no_of_labels: Int = ApplicationConfig.Default.metrics.no_of_labels

  override def getMetricKeys(): Seq[String] = ApplicationConfig.Default.metrics.labelled_keys

  override def getMetricLableNames() = {
    ApplicationConfig.Default.metrics.label_names.padTo(no_of_labels,"").take(no_of_labels)
  }

  def startMessage(labels: Seq[String]) = super.startMessage(getMetricKeys(), labels)
  def completedMessage(processingTime: Long, labels: Seq[String]) = super.completedMessage(processingTime, getMetricKeys(), labels)
  def erroredMessage(processingTime: Long, labels: Seq[String]): Unit = super.erroredMessage(processingTime, getMetricKeys(), labels)

  override final protected def getMetrics(keys: Seq[String], labels: Seq[String]): MetricValue = super.getMetrics(keys, labels)

}
