package vdi.components.metrics

import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Histogram

class ScriptMetrics(registry: CollectorRegistry) {

  val importScriptDuration: Histogram = Histogram.build()
    .name("script_import_duration")
    .help("Import script duration in seconds.")
    .buckets(
      0.1,   // 100ms
      0.5,   // 500ms
      1.0,   // 1s
      5.0,   // 5s
      10.0,  // 10s
      30.0,  // 30s
      60.0,  // 1m
      300.0, // 5m
    )
    .register(registry)

  val installMetaScriptDuration: Histogram = Histogram.build()
    .name("script_install_meta_duration")
    .help("Install-Meta script duration in milliseconds")
    .buckets(
      0.1,   // 100ms
      0.5,   // 500ms
      1.0,   // 1s
      5.0,   // 5s
      10.0,  // 10s
      30.0,  // 30s
      60.0,  // 1m
      300.0, // 5m
    )
    .register(registry)

  val installDataScriptDuration: Histogram = Histogram.build()
    .name("script_install_data_duration")
    .help("Install-Data script duration in milliseconds")
    .buckets(
      5.0,    // 5s
      10.0,   // 10s
      30.0,   // 30s
      60.0,   // 1m
      120.0,  // 2m
      300.0,  // 5m
      600.0,  // 10m
      900.0,  // 15m
      1800.0, // 30m
    )
    .register(registry)

  val uninstallScriptDuration: Histogram = Histogram.build()
    .name("script_uninstall_duration")
    .help("Uninstall script duration in milliseconds")
    .buckets(
      0.1,   // 100ms
      0.5,   // 500ms
      1.0,   // 1s
      5.0,   // 5s
      10.0,  // 10s
      30.0,  // 30s
      60.0,  // 1m
      300.0, // 5m
    )
    .register(registry)
}