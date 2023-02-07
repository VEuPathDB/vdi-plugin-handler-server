package vdi

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.slf4j.LoggerFactory
import vdi.components.ldap.LDAP
import vdi.components.ldap.LDAPConfig
import vdi.components.ldap.LDAPHost
import vdi.components.script.ScriptExecutor
import vdi.components.script.ScriptExecutorImpl
import vdi.conf.Configuration
import vdi.conf.Configuration.ServerConfiguration
import vdi.conf.printToLogs
import vdi.conf.validate
import vdi.server.configureRouting
import vdi.util.setupLDAP

fun main() {
  val log = LoggerFactory.getLogger("main")

  Configuration.validate()
  Configuration.printToLogs(log)

  val ldap = setupLDAP(Configuration.ServiceConfiguration)

  embeddedServer(
    Netty,
    port = ServerConfiguration.port.toInt(),
    host = ServerConfiguration.host,
    module = { configureRouting(ldap) }
  ).start(true)
}
