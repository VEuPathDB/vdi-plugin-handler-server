package vdi.model

import io.prometheus.metrics.model.registry.PrometheusRegistry
import vdi.components.metrics.ScriptMetrics

data class MetricsBundle(
  val prometheus:    PrometheusRegistry,
  val scriptMetrics: ScriptMetrics,
)
