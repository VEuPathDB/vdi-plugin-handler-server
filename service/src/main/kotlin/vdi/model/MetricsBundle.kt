package vdi.model

import io.micrometer.prometheus.PrometheusMeterRegistry
import vdi.components.metrics.ScriptMetrics

data class MetricsBundle(
  val micrometer:    PrometheusMeterRegistry,
  val scriptMetrics: ScriptMetrics,
)
