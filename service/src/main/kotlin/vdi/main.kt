package vdi

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.slf4j.LoggerFactory
import vdi.components.script.ScriptExecutorImpl
import vdi.conf.Configuration
import vdi.conf.printToLogs
import vdi.conf.validate
import vdi.server.configureRouting
import vdi.util.setupLDAP

fun main() {
  val log = LoggerFactory.getLogger("main")

  val config = Configuration()
  config.validate()
  config.printToLogs(log)

  val ldap = setupLDAP(config.service)
  val exec = ScriptExecutorImpl()

  embeddedServer(
    Netty,
    port = config.server.port.toInt(),
    host = config.server.host,
    module = { configureRouting(config, ldap, exec) }
  ).start(true)
}
