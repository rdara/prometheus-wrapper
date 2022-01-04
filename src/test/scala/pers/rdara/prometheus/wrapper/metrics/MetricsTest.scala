package pers.rdara.prometheus.wrapper.metrics

import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers, PrivateMethodTester}
import pers.rdara.akka.http.test.server.services.Metrics
/**
 * @author Ramesh Dara
 * @since Jun-2019
 */
class MetricsTest extends FlatSpec with Matchers with PrivateMethodTester with  BeforeAndAfterEach {

  val defaultMetric: Metrics = DefaultMetrics
  val metricKey: String = "group_module_test"
  val getMetrics = PrivateMethod[(MetricValue)]('getMetrics)
  val metricValue = defaultMetric invokePrivate getMetrics(Seq(metricKey))

  var baseTotalMessages = 0
  var baseInflightMessages = 0
  var baseProcessingTimeCount = 0
  var baseProcessingTimeSum = 0
  var baseActiveMessageCount = 0
  var baseTotalMessageCount = 0

  override def beforeEach(): Unit = {
    baseTotalMessages = metricValue.total.get.toInt
    baseInflightMessages = metricValue.inflight.get.toInt
    baseProcessingTimeCount =  metricValue.processingTime.get().count.toInt
    baseProcessingTimeSum = metricValue.processingTime.get().sum.toInt
    baseActiveMessageCount = defaultMetric.getActiveMessagesCount
    baseTotalMessageCount = defaultMetric.getTotalMessagesCount
  }

  "startMessage" should "correctly count 0  messages" in {
    metricValue.total.get - baseTotalMessages shouldBe 0
    metricValue.inflight.get - baseInflightMessages shouldBe 0
    metricValue.processingTime.get().count - baseProcessingTimeCount shouldBe 0
  }

  it should "correctly count 1  message" in {
    defaultMetric.startMessage(metricKey)

    defaultMetric.getActiveMessagesCount - baseActiveMessageCount shouldBe 1
    defaultMetric.getTotalMessagesCount - baseTotalMessageCount shouldBe 1


    metricValue.total.get - baseTotalMessages shouldBe 1
    metricValue.inflight.get - baseInflightMessages shouldBe 1
    metricValue.processingTime.get().count - baseProcessingTimeCount shouldBe 0
  }

  it should "correctly count 2 messages" in {
    defaultMetric.startMessage(metricKey)
    defaultMetric.startMessage(metricKey)

    defaultMetric.getActiveMessagesCount - baseActiveMessageCount shouldBe 2
    defaultMetric.getTotalMessagesCount - baseTotalMessageCount shouldBe 2

    metricValue.total.get - baseTotalMessages shouldBe 2
    metricValue.inflight.get - baseInflightMessages shouldBe 2
    metricValue.processingTime.get().count - baseProcessingTimeCount shouldBe 0
  }

  "completedMessage" should "correctly count 0  messages" in {
    metricValue.total.get - baseTotalMessages shouldBe 0
    metricValue.inflight.get - baseInflightMessages shouldBe 0
    metricValue.processingTime.get().count - baseProcessingTimeCount shouldBe 0
  }

  it should "correctly count 1  message" in {
    defaultMetric.startMessage(metricKey)
    defaultMetric.completedMessage(10, metricKey)

    metricValue.total.get - baseTotalMessages shouldBe 1
    metricValue.inflight.get - baseInflightMessages shouldBe 0
    metricValue.processingTime.get().count - baseProcessingTimeCount shouldBe 1
    metricValue.processingTime.get().sum - baseProcessingTimeSum shouldBe 10
  }

  it should "correctly count 2 messages" in {
    defaultMetric.startMessage(metricKey)
    defaultMetric.startMessage(metricKey)

    defaultMetric.completedMessage(10, metricKey)
    defaultMetric.completedMessage(20, metricKey)

    metricValue.total.get - baseTotalMessages shouldBe 2
    metricValue.inflight.get - baseInflightMessages shouldBe 0
    metricValue.processingTime.get().count - baseProcessingTimeCount shouldBe 2
    metricValue.processingTime.get().sum - baseProcessingTimeSum shouldBe 30
  }

  "erroredMessage" should "be correctly count 1 message" in {

    val errorMetricValue = defaultMetric invokePrivate getMetrics(Seq(metricKey, "errored"))

    errorMetricValue.total.get shouldBe 0
    errorMetricValue.inflight.get shouldBe 0
    errorMetricValue.processingTime.get().count shouldBe 0

    defaultMetric.startMessage(metricKey)
    defaultMetric.erroredMessage(10, metricKey)

    errorMetricValue.total.get shouldBe 1
    errorMetricValue.inflight.get shouldBe 0
    errorMetricValue.processingTime.get().count shouldBe 1
    errorMetricValue.processingTime.get().sum shouldBe 10

    metricValue.total.get - baseTotalMessages shouldBe 1
    metricValue.inflight.get - baseInflightMessages shouldBe 0
    metricValue.processingTime.get().count - baseProcessingTimeCount shouldBe 1
    metricValue.processingTime.get().sum - baseProcessingTimeSum shouldBe 10
 }

  "No duplicate registrations" should "occur with the same keys" in {

    defaultMetric.startMessage("outbound","email", "dup")
    defaultMetric.startMessage("outbound","email", "dup")
    defaultMetric.startMessage("outbound","email", "dup")

    val dupMetricValue = defaultMetric invokePrivate getMetrics(Seq("outbound","email", "dup"))

    dupMetricValue.total.get shouldBe 3
  }

  "Concurrent registrations" should "work as expected" in {

   (1 to 50).par.foreach {
      _ => defaultMetric.startMessage("outbound","email", "par")
    }

    val dupMetricValue = defaultMetric invokePrivate getMetrics(Seq("outbound","email", "par"))

    dupMetricValue.total.get.toInt shouldBe 50
  }
}
