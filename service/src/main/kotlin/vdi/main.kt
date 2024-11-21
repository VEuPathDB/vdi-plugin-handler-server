package vdi

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.micrometer.core.instrument.Clock
import io.micrometer.core.instrument.Metrics
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.prometheus.metrics.model.registry.PrometheusRegistry
import org.slf4j.LoggerFactory
import vdi.components.metrics.ScriptMetrics
import vdi.components.script.ScriptExecutorImpl
import vdi.conf.HandlerConfig
import vdi.conf.printToLogs
import vdi.conf.validate
import vdi.model.ApplicationContext
import vdi.model.MetricsBundle
import vdi.server.configureServer
import vdi.util.DatasetPathFactory
import vdi.util.setupLDAP

fun main() {
  val log = LoggerFactory.getLogger("main")

  log.debug("loading configuration")
  val config = HandlerConfig()

  log.debug("validating configuration")
  config.validate()
  config.printToLogs(log)

  val appCtx = ApplicationContext(
    config,
    setupLDAP(config.service),
    ScriptExecutorImpl(),
    PrometheusRegistry.defaultRegistry
      .also { Metrics.globalRegistry.add(PrometheusMeterRegistry(PrometheusConfig.DEFAULT, it, Clock.SYSTEM)) }
      .let { MetricsBundle(it, ScriptMetrics(it)) },
    DatasetPathFactory(config.service.datasetRoot, config.service.siteBuild)
  )

  log.debug("starting embedded server")
  embeddedServer(
    Netty,
    port = config.server.port.toInt(),
    host = config.server.host,
    module = { configureServer(appCtx) }
  ).start(true)
}
