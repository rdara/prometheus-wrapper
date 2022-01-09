package pers.rdara.prometheus.wrapper.metrics

import com.typesafe.scalalogging.LazyLogging
import io.prometheus.client.{Counter, Gauge, Summary}
import pers.rdara.prometheus.wrapper.metrics.interfaces.MetricsInterface

import scala.collection.concurrent.TrieMap


/**
 * @author Ramesh Dara
 * @since Jun-2019
 */
abstract class AbstractMetrics extends MetricsInterface with LazyLogging {

    //Mitigate the case if prometheus libraries are not packaged or available at runtime
    private val isPrometheusAvailable: Boolean =
      try {
        Gauge.build
        true
      } catch {
        case e: Throwable => logger.info("Metrics are not available and hence skipping all prometheus metrics collection.", e)
        false
      }

  private val metrics = TrieMap.empty[String, MetricValue]
  private lazy val activeRequestsCounter: Gauge = Gauge.build("requests","requests").create()
  private lazy val totalRequestsCounter: Counter = Counter.build("total_requests","total requests").create()

  /*
   * The custom metrics are generated on demand. Meaning the metrics are available only when the function has been used.
   * The metrics (wavefront) dashboards treats non-availability of metrics as error. If in case the application is not started and there will be
   * no metrics.
   *
   * In order to support dynamic metrics, its better register at least one face of various metrics that are generated.
   * For Example, make the calls like for outbound email channel metrics
   *  DefaultMetrics.registerMetrics("outbound","email")
   *  DefaultMetrics.registerMetrics("outbound","email", "error"))
   */

  private def registerMetrics(keys: Seq[String], labels: Seq[String]): Unit = if (isPrometheusAvailable) {
    getMetrics(keys, labels)
  }

  /*
  Get the metric for a given key.
  If the key is not registered, then the key is registered and then returns the corresponding MetricValue.
  For every key, there will be 3 different metrics:
    1. total_messages     : Total number
    2. inflight_messages  : At any given time, how many messages got initiated and not yet completed.
    3. processing_time    : How long it takes to complete a message/request.
  The following statement would have been simpler, but didnt use as its not thread safe operation
  metrics.getOrElseUpdate(metricKey, getRegisteredMetric())
   */
  protected def getMetrics(keys: Seq[String], labels: Seq[String]): MetricValue = {
    //val metricKey:String = (getNonNullSeq(keys) ++ getNonNullSeq(labels)).mkString("_")
    val metricKey:String = (getNonNullSeq(keys)).mkString("_")
    def getMetricName(suffix: String) = s"${getNonNullSeq(keys).mkString("_")}_${suffix}".toLowerCase
    def getRegisteredMetric() =  {
      if(metrics.get(metricKey) == None) {
        synchronized {
          if (metrics.get(metricKey) == None) {
            val totalCounter: Counter = Counter
              .build()
              .labelNames(getMetricLableNames(): _*)
              .name(getMetricName("total_messages"))
              .help("Total number of messages received. Resets to zero upon application restart.")
              .register()
            val inflightGuage: Gauge = Gauge
              .build()
              .name(getMetricName("inflight_messages"))
              .labelNames(getMetricLableNames(): _*)
              .help("Total number of messages under progress. Active messages count.")
              .register()
            val processingTimeSummary: Summary = Summary
              .build()
              .name(getMetricName("processing_time"))
              .labelNames(getMetricLableNames(): _*)
              .help("Message processing time in MilliSeconds.")
              .register()
            metrics.putIfAbsent(metricKey, MetricValue(totalCounter, inflightGuage, processingTimeSummary))
            logger.info(s"Registered the prometheus metrics (1): ${getMetricName("total_messages")}")
            logger.info(s"Registered the prometheus metrics (2): ${getMetricName("inflight_messages")}")
            logger.info(s"Registered the prometheus metrics (3): ${getMetricName("processing_time")}")
          }
        }
      }
      metrics.get(metricKey).get
    }
    getRegisteredMetric
  }

  // A message/request metric start and hence increment total messages and in_flight messages
  // This is effective only if prometheus libs are available in class path
  def startMessage(keys: Seq[String], labels: Seq[String]): Unit = if (isPrometheusAvailable) {
    activeRequestsCounter.inc()
    totalRequestsCounter.inc()
    val metricValue = getMetrics(keys, labels)
    metricValue.total.labels(labels: _*).inc()
    metricValue.inflight.labels(labels: _*).inc()
  }

  // A message/request metric completed and hence record processing time and decrease in flight messages
  // This is effective only if prometheus libs are available in class path
  def completedMessage(processingTime: Long, keys: Seq[String], labels: Seq[String]): Unit = if (isPrometheusAvailable) {
    activeRequestsCounter.dec()
    val metricValue = getMetrics(keys, labels)
    metricValue.inflight.labels(labels: _*).dec()
    metricValue.processingTime.labels(labels: _*).observe(processingTime)
  }

  //An error occurred. And record the error metric.
  // This is effective only if prometheus libs are available in class path
  def erroredMessage(processingTime: Long, keys: Seq[String], labels: Seq[String]): Unit = if (isPrometheusAvailable) {
    activeRequestsCounter.dec()
    val metricValue = getMetrics(keys, labels)
    metricValue.inflight.labels(labels: _*)dec()
    metricValue.processingTime.labels(labels: _*).observe(processingTime)

    val errorMetricValue = getMetrics(keys :+ "errored", labels)
    errorMetricValue.total.labels(labels: _*).inc()
    errorMetricValue.processingTime.labels(labels: _*).observe(processingTime)
  }

  // Gets in active metrics count of entire application at the given time.
  // This is effective only if prometheus libs are available in class path
  def getActiveMessagesCount: Int = if (isPrometheusAvailable) {
    activeRequestsCounter.get().toInt
  } else {
    0
  }

  // Total requests for entire application at the given time
  // This is effective only if prometheus libs are available in class path
  def getTotalMessagesCount: Int = if (isPrometheusAvailable) {
    totalRequestsCounter.get().toInt
  } else {
    0
  }

  def getNonNullSeq(seq: Seq[String]): Seq[String] = {
    if(seq == null) {
      Seq.empty[String]
    } else {
      seq
    }
  }

}

