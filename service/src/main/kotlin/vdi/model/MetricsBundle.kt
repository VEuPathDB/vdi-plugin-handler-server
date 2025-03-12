package vdi.model

import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import vdi.components.metrics.ScriptMetrics

data class MetricsBundle(
  val micrometer:    PrometheusMeterRegistry,
  val scriptMetrics: ScriptMetrics,
)
