package pers.rdara.prometheus.wrapper.metrics

import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FlatSpec, Matchers, PrivateMethodTester}

/**
 * @author Ramesh Dara
 * @since Jun-2019
 */
class LabelledMetricsTest extends FlatSpec with Matchers with PrivateMethodTester with  BeforeAndAfterEach with BeforeAndAfterAll {

  System.setProperty("application.metrics.no_of_labels","4")
  System.setProperty("application.metrics.label_names", "label_1,label_2,label_3,label_4")

  val labelledMetrics: AbstractMetrics = LabelledMetrics
  val metricKeys: Seq[String] = Seq("group","module","test","labelled")
  val labelValues: Seq[String] = Seq("value_1","value_2","value_3","value-4")
  val getMetrics = PrivateMethod[(MetricValue)]('getMetrics)
  val metricValue =  labelledMetrics invokePrivate getMetrics(metricKeys, labelValues)

  var baseTotalMessages = 0
  var baseInflightMessages = 0
  var baseProcessingTimeCount = 0
  var baseProcessingTimeSum = 0
  var baseActiveMessageCount = 0
  var baseTotalMessageCount = 0

  var prior_no_of_labels = ""
  var prior_label_names = ""

  override def beforeAll(): Unit = {
    prior_no_of_labels = System.getProperty("application.metrics.no_of_labels")
    prior_no_of_labels = System.getProperty("application.metrics.label_names")
  }

  override def afterAll(): Unit = {
    System.setProperty("application.metrics.no_of_labels", prior_no_of_labels)
    System.setProperty("application.metrics.label_names", prior_label_names)
  }

  override def beforeEach(): Unit = {
    baseTotalMessages = metricValue.total.labels(labelValues: _*).get.toInt
    baseInflightMessages = metricValue.inflight.labels(labelValues: _*).get.toInt
    baseProcessingTimeCount =  metricValue.processingTime.labels(labelValues: _*).get().count.toInt
    baseProcessingTimeSum = metricValue.processingTime.labels(labelValues: _*).get().sum.toInt
    baseActiveMessageCount = labelledMetrics.getActiveMessagesCount
    baseTotalMessageCount = labelledMetrics.getTotalMessagesCount
  }

  "startMessage" should "correctly count 0  messages" in {
    metricValue.total.labels(labelValues: _*).get - baseTotalMessages shouldBe 0
    metricValue.inflight.labels(labelValues: _*).get - baseInflightMessages shouldBe 0
    metricValue.processingTime.labels(labelValues: _*).get().count - baseProcessingTimeCount shouldBe 0
  }

  //The following tests have their own exclusive metric key sets and doenst depend upon beforeEach values
  it should "correctly count 1  message with null keyset" in {
    val curMetricKeySet = null
    val priorMetricValue = labelledMetrics invokePrivate getMetrics(curMetricKeySet, labelValues)
    val priorTotal = priorMetricValue.total.labels(labelValues: _*).get.toInt
    labelledMetrics.startMessage(curMetricKeySet, labelValues)

    labelledMetrics.getActiveMessagesCount - baseActiveMessageCount shouldBe 1
    labelledMetrics.getTotalMessagesCount - baseTotalMessageCount shouldBe 1

    val currentMetricValue = labelledMetrics invokePrivate getMetrics(curMetricKeySet, labelValues)
    currentMetricValue.total.labels(labelValues: _*).get - priorTotal shouldBe 1
  }

  it should "correctly count 1  message with empty keyset" in {
    val curMetricKeySet = Seq.empty[String]
    val priorMetricValue = labelledMetrics invokePrivate getMetrics(curMetricKeySet, labelValues)
    val priorTotal = priorMetricValue.total.labels(labelValues: _*).get.toInt
    labelledMetrics.startMessage(curMetricKeySet, labelValues)

    labelledMetrics.getActiveMessagesCount - baseActiveMessageCount shouldBe 1
    labelledMetrics.getTotalMessagesCount - baseTotalMessageCount shouldBe 1

    val currentMetricValue = labelledMetrics invokePrivate getMetrics(curMetricKeySet, labelValues)
    currentMetricValue.total.labels(labelValues: _*).get.toInt - priorTotal shouldBe 1
  }

  it should "correctly count 1  message with keyset with null values" in {
    val curMetricKeySet = metricKeys :+ null
    val priorMetricValue = labelledMetrics invokePrivate getMetrics(curMetricKeySet, labelValues)
    val priorTotal = priorMetricValue.total.labels(labelValues: _*).get.toInt
    labelledMetrics.startMessage(curMetricKeySet, labelValues)

    labelledMetrics.getActiveMessagesCount - baseActiveMessageCount shouldBe 1
    labelledMetrics.getTotalMessagesCount - baseTotalMessageCount shouldBe 1

    val currentMetricValue = labelledMetrics invokePrivate getMetrics(curMetricKeySet, labelValues)
    currentMetricValue.total.labels(labelValues: _*).get - priorTotal shouldBe 1
  }

  //The following tests ban upon the same metricKeys and hence before each values
  it should "correctly count 1  message" in {
    labelledMetrics.startMessage(metricKeys, labelValues)

    labelledMetrics.getActiveMessagesCount - baseActiveMessageCount shouldBe 1
    labelledMetrics.getTotalMessagesCount - baseTotalMessageCount shouldBe 1

    metricValue.total.labels(labelValues: _*).get - baseTotalMessages shouldBe 1
    metricValue.inflight.labels(labelValues: _*).get - baseInflightMessages shouldBe 1
    metricValue.processingTime.labels(labelValues: _*).get().count - baseProcessingTimeCount shouldBe 0
  }

  it should "correctly count 2 messages" in {
    labelledMetrics.startMessage(metricKeys, labelValues)
    labelledMetrics.startMessage(metricKeys, labelValues)

    labelledMetrics.getActiveMessagesCount - baseActiveMessageCount shouldBe 2
    labelledMetrics.getTotalMessagesCount - baseTotalMessageCount shouldBe 2

    metricValue.total.labels(labelValues: _*).get - baseTotalMessages shouldBe 2
    metricValue.inflight.labels(labelValues: _*).get - baseInflightMessages shouldBe 2
    metricValue.processingTime.labels(labelValues: _*).get().count - baseProcessingTimeCount shouldBe 0
  }

  "completedMessage" should "correctly count 0  messages" in {
    metricValue.total.labels(labelValues: _*).get - baseTotalMessages shouldBe 0
    metricValue.inflight.labels(labelValues: _*).get - baseInflightMessages shouldBe 0
    metricValue.processingTime.labels(labelValues: _*).get().count - baseProcessingTimeCount shouldBe 0
  }

  it should "correctly count 1  message" in {
    labelledMetrics.startMessage(metricKeys, labelValues)
    labelledMetrics.completedMessage(10, metricKeys,labelValues)

    metricValue.total.labels(labelValues: _*).get - baseTotalMessages shouldBe 1
    metricValue.inflight.labels(labelValues: _*).get - baseInflightMessages shouldBe 0
    metricValue.processingTime.labels(labelValues: _*).get().count - baseProcessingTimeCount shouldBe 1
    metricValue.processingTime.labels(labelValues: _*).get().sum - baseProcessingTimeSum shouldBe 10
  }

  it should "correctly count 2 messages" in {
    labelledMetrics.startMessage(metricKeys, labelValues)
    labelledMetrics.startMessage(metricKeys, labelValues)

    labelledMetrics.completedMessage(10, metricKeys,labelValues)
    labelledMetrics.completedMessage(20, metricKeys,labelValues)

    metricValue.total.labels(labelValues: _*).get - baseTotalMessages shouldBe 2
    metricValue.inflight.labels(labelValues: _*).get - baseInflightMessages shouldBe 0
    metricValue.processingTime.labels(labelValues: _*).get().count - baseProcessingTimeCount shouldBe 2
    metricValue.processingTime.labels(labelValues: _*).get().sum - baseProcessingTimeSum shouldBe 30
  }

  "erroredMessage" should "be correctly count 1 message" in {

    val errorMetricValue = labelledMetrics invokePrivate getMetrics(metricKeys :+ "errored", labelValues)

    errorMetricValue.total.labels(labelValues: _*).get shouldBe 0
    errorMetricValue.inflight.labels(labelValues: _*).get shouldBe 0
    errorMetricValue.processingTime.labels(labelValues: _*).get().count shouldBe 0

    labelledMetrics.startMessage(metricKeys, labelValues)
    labelledMetrics.erroredMessage(10, metricKeys,labelValues)

    errorMetricValue.total.labels(labelValues: _*).get shouldBe 1
    errorMetricValue.inflight.labels(labelValues: _*).get shouldBe 0
    errorMetricValue.processingTime.labels(labelValues: _*).get().count shouldBe 1
    errorMetricValue.processingTime.labels(labelValues: _*).get().sum shouldBe 10

    metricValue.total.labels(labelValues: _*).get - baseTotalMessages shouldBe 1
    metricValue.inflight.labels(labelValues: _*).get - baseInflightMessages shouldBe 0
    metricValue.processingTime.labels(labelValues: _*).get().count - baseProcessingTimeCount shouldBe 1
    metricValue.processingTime.labels(labelValues: _*).get().sum - baseProcessingTimeSum shouldBe 10
 }

  "No duplicate registrations" should "occur with the same keys" in {

    labelledMetrics.startMessage(Seq("outbound","email", "dup", "labelled"), labelValues)
    labelledMetrics.startMessage(Seq("outbound","email", "dup", "labelled"), labelValues)
    labelledMetrics.startMessage(Seq("outbound","email", "dup", "labelled"), labelValues)

    val dupMetricValue = labelledMetrics invokePrivate getMetrics(Seq("outbound","email", "dup", "labelled"), labelValues)

    dupMetricValue.total.labels(labelValues: _*).get shouldBe 3
  }

  "Concurrent registrations" should "work as expected" in {

   (1 to 50).par.foreach {
      _ => labelledMetrics.startMessage(Seq("outbound","email", "par", "labelled"), labelValues)
    }

    val dupMetricValue = labelledMetrics invokePrivate getMetrics(Seq("outbound","email", "par", "labelled"), labelValues)

    dupMetricValue.total.labels(labelValues: _*).get.toInt shouldBe 50
  }
}
