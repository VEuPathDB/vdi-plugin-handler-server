package vdi

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.micrometer.prometheusmetrics.PrometheusConfig
import org.slf4j.LoggerFactory
import org.veupathdb.lib.ldap.LDAP
import org.veupathdb.lib.ldap.LDAPConfig
import org.veupathdb.lib.ldap.LDAPHost
import kotlin.io.path.Path
import vdi.components.metrics.ScriptMetrics
import vdi.components.script.ScriptExecutorImpl
import vdi.conf.HandlerConfig
import vdi.conf.loadServiceConfig
import vdi.consts.ConfigDefault
import vdi.model.ApplicationContext
import vdi.model.MetricsBundle
import vdi.server.configureServer
import vdi.util.DatasetPathFactory

fun main() {
  val log = LoggerFactory.getLogger("main")

  log.debug("loading configuration")
  val config = loadServiceConfig(Path("/etc/vdi/config.yml"))

  val appCtx = ApplicationContext(
    HandlerConfig(config),
    LDAP(LDAPConfig(config.ldap.servers.map { LDAPHost(it.host, it.port ?: 389u) }, config.ldap.baseDN)),
    ScriptExecutorImpl(),
    PrometheusMeterRegistry(PrometheusConfig.DEFAULT).let { MetricsBundle(it, ScriptMetrics(it)) },
    DatasetPathFactory(config.installRoot ?: "/datasets", config.siteBuild)
  )

  log.debug("starting embedded server")
  embeddedServer(
    Netty,
    port = config.http.port.toInt(),
    host = ConfigDefault.ServerHost,
    module = { configureServer(appCtx) }
  ).start(true)
}
