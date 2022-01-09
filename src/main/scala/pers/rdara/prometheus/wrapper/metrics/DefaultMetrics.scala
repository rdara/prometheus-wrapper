package pers.rdara.prometheus.wrapper.metrics

import pers.rdara.akka.http.test.server.common.ApplicationConfig


/**
 * @author Ramesh Dara
 * @since Jun-2019
 */
object DefaultMetrics extends AbstractMetrics {
  override def getMetricLableNames() = Seq.empty[String]
  override def getMetricKeys(): Seq[String] = ApplicationConfig.Default.metrics.default_keys
  private def getMetrics(keys: Seq[String]): MetricValue = super.getMetrics(keys, Seq.empty[String])
}

