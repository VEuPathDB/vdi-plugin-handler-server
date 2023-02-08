package vdi.server

import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import vdi.components.http.errors.withExceptionMapping
import vdi.components.ldap.LDAP
import vdi.components.script.ScriptExecutor
import vdi.conf.HandlerConfig
import vdi.server.controller.*


fun Application.configureRouting(
  config:   HandlerConfig,
  ldap:     LDAP,
  executor: ScriptExecutor,
) {
  val micrometer = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

  install(MicrometerMetrics) { registry = micrometer }

  routing {
    post("/import") { withExceptionMapping { call.handleImportRequest(executor, config.service.importScript) } }

    route("/install") {
      post("/data") { withExceptionMapping { call.handleInstallDataRequest(config, ldap, executor) } }
      post("/meta") { withExceptionMapping { call.handleInstallMetaRequest(config, ldap, executor) } }
    }

    post("/uninstall") { withExceptionMapping { call.handleUninstallRequest(executor, ldap, config) } }

    get("/metrics") { call.respond(micrometer.scrape()) }
  }
}
