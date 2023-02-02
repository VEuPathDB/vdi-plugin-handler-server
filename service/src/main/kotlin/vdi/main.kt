package vdi

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.slf4j.LoggerFactory
import vdi.conf.Configuration
import vdi.conf.Configuration.ServerConfiguration
import vdi.conf.printToLogs
import vdi.conf.validate
import vdi.server.configureRouting

fun main() {
  val log = LoggerFactory.getLogger("main")

  Configuration.validate()
  Configuration.printToLogs(log)

  embeddedServer(
    Netty,
    port = ServerConfiguration.port.toInt(),
    host = ServerConfiguration.host,
    module = Application::module
  ).start(true)
}

fun Application.module() {
  configureRouting()
}
