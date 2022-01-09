package pers.rdara.prometheus.wrapper.metrics

import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers, PrivateMethodTester}
import pers.rdara.prometheus.wrapper.metrics.interfaces.MetricsInterface
/**
 * @author Ramesh Dara
 * @since Jun-2019
 */
class DefaultMetricsTest extends FlatSpec with Matchers with PrivateMethodTester with  BeforeAndAfterEach {

  val defaultMetric: AbstractMetrics = DefaultMetrics
  val metricKeys: Seq[String] = Seq("group","module","test")
  val getMetrics = PrivateMethod[(MetricValue)]('getMetrics)
  val metricValue = defaultMetric invokePrivate getMetrics(metricKeys)
//  val metricValue =  defaultMetric invokePrivate getMetrics(metricKeys, Seq.empty[String])

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

  //The following tests have their own exclusive metric key sets and doenst depend upon beforeEach values
  it should "correctly count 1  message with null keyset" in {
    val curMetricKeySet = null
    val priorMetricValue = defaultMetric invokePrivate getMetrics(curMetricKeySet)
    val priorTotal = priorMetricValue.total.get.toInt
    defaultMetric.startMessage(curMetricKeySet)

    defaultMetric.getActiveMessagesCount - baseActiveMessageCount shouldBe 1
    defaultMetric.getTotalMessagesCount - baseTotalMessageCount shouldBe 1

    val currentMetricValue = defaultMetric invokePrivate getMetrics(curMetricKeySet)
    currentMetricValue.total.get - priorTotal shouldBe 1
  }

  it should "correctly count 1  message with empty keyset" in {
    val curMetricKeySet = Seq.empty[String]
    val priorMetricValue = defaultMetric invokePrivate getMetrics(curMetricKeySet)
    val priorTotal = priorMetricValue.total.get.toInt
    defaultMetric.startMessage(curMetricKeySet)

    defaultMetric.getActiveMessagesCount - baseActiveMessageCount shouldBe 1
    defaultMetric.getTotalMessagesCount - baseTotalMessageCount shouldBe 1

    val currentMetricValue = defaultMetric invokePrivate getMetrics(curMetricKeySet)
    currentMetricValue.total.get.toInt - priorTotal shouldBe 1
  }

  it should "correctly count 1  message with keyset with null values" in {
    val curMetricKeySet = metricKeys :+ null
    val priorMetricValue = defaultMetric invokePrivate getMetrics(curMetricKeySet)
    val priorTotal = priorMetricValue.total.get.toInt
    defaultMetric.startMessage(curMetricKeySet)

    defaultMetric.getActiveMessagesCount - baseActiveMessageCount shouldBe 1
    defaultMetric.getTotalMessagesCount - baseTotalMessageCount shouldBe 1

    val currentMetricValue = defaultMetric invokePrivate getMetrics(curMetricKeySet)
    currentMetricValue.total.get - priorTotal shouldBe 1
  }

  //The following tests ban upon the same metricKeys and hence before each values
  it should "correctly count 1  message" in {
    defaultMetric.startMessage(metricKeys)

    defaultMetric.getActiveMessagesCount - baseActiveMessageCount shouldBe 1
    defaultMetric.getTotalMessagesCount - baseTotalMessageCount shouldBe 1

    metricValue.total.get - baseTotalMessages shouldBe 1
    metricValue.inflight.get - baseInflightMessages shouldBe 1
    metricValue.processingTime.get().count - baseProcessingTimeCount shouldBe 0
  }

  it should "correctly count 2 messages" in {
    defaultMetric.startMessage(metricKeys)
    defaultMetric.startMessage(metricKeys)

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
    defaultMetric.startMessage(metricKeys)
    defaultMetric.completedMessage(10, metricKeys)

    metricValue.total.get - baseTotalMessages shouldBe 1
    metricValue.inflight.get - baseInflightMessages shouldBe 0
    metricValue.processingTime.get().count - baseProcessingTimeCount shouldBe 1
    metricValue.processingTime.get().sum - baseProcessingTimeSum shouldBe 10
  }

  it should "correctly count 2 messages" in {
    defaultMetric.startMessage(metricKeys)
    defaultMetric.startMessage(metricKeys)

    defaultMetric.completedMessage(10, metricKeys)
    defaultMetric.completedMessage(20, metricKeys)

    metricValue.total.get - baseTotalMessages shouldBe 2
    metricValue.inflight.get - baseInflightMessages shouldBe 0
    metricValue.processingTime.get().count - baseProcessingTimeCount shouldBe 2
    metricValue.processingTime.get().sum - baseProcessingTimeSum shouldBe 30
  }

  "erroredMessage" should "be correctly count 1 message" in {

    val errorMetricValue = defaultMetric invokePrivate getMetrics(metricKeys :+ "errored")

    errorMetricValue.total.get shouldBe 0
    errorMetricValue.inflight.get shouldBe 0
    errorMetricValue.processingTime.get().count shouldBe 0

    defaultMetric.startMessage(metricKeys)
    defaultMetric.erroredMessage(10, metricKeys)

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

    defaultMetric.startMessage(Seq("outbound","email", "dup"))
    defaultMetric.startMessage(Seq("outbound","email", "dup"))
    defaultMetric.startMessage(Seq("outbound","email", "dup"))

    val dupMetricValue = defaultMetric invokePrivate getMetrics(Seq("outbound","email", "dup"))

    dupMetricValue.total.get shouldBe 3
  }

  "Concurrent registrations" should "work as expected" in {

   (1 to 50).par.foreach {
      _ => defaultMetric.startMessage(Seq("outbound","email", "par"))
    }

    val dupMetricValue = defaultMetric invokePrivate getMetrics(Seq("outbound","email", "par"))

    dupMetricValue.total.get.toInt shouldBe 50
  }
}
