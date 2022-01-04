package pers.rdara.prometheus.wrapper.metrics

import io.prometheus.client.{Counter, Gauge, Summary}

case class MetricValue(total: Counter, inflight: Gauge, processingTime: Summary)
