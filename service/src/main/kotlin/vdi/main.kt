package vdi

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.slf4j.LoggerFactory
import vdi.components.script.ScriptExecutorImpl
import vdi.conf.HandlerConfig
import vdi.conf.printToLogs
import vdi.conf.validate
import vdi.server.configureRouting
import vdi.util.setupLDAP

fun main() {
  val log = LoggerFactory.getLogger("main")

  log.debug("loading configuration")
  val config = HandlerConfig()

  log.debug("validating configuration")
  config.validate()

  config.printToLogs(log)

  log.debug("connecting to LDAP")
  val ldap = setupLDAP(config.service)

  val exec = ScriptExecutorImpl()

  log.debug("starting embedded server")
  embeddedServer(
    Netty,
    port = config.server.port.toInt(),
    host = config.server.host,
    module = { configureRouting(config, ldap, exec) }
  ).start(true)
}
