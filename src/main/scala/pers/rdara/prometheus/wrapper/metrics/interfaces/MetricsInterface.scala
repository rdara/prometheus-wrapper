package pers.rdara.prometheus.wrapper.metrics.interfaces

import pers.rdara.akka.http.test.server.common.ApplicationConfig

trait MetricsInterface {
  def startMessage(keys: Seq[String] = getMetricKeys(), labels: Seq[String] = Seq.empty[String])
  def completedMessage(processingTime: Long, keys: Seq[String] = getMetricKeys(), labels: Seq[String] = Seq.empty[String]): Unit
  def erroredMessage(processingTime: Long, keys: Seq[String] = getMetricKeys(), labels: Seq[String] = Seq.empty[String]): Unit

  def getActiveMessagesCount: Int
  def getTotalMessagesCount: Int

  def getMetricKeys(): Seq[String]

  def getMetricLableNames(): Seq[String]
}

