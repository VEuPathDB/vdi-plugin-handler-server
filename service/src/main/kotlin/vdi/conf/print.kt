package vdi.conf

import org.slf4j.Logger
import kotlin.time.Duration.Companion.seconds

fun HandlerConfig.printToLogs(log: Logger) {
  val sb = StringBuilder(4096)

  sb.append("Configuration:\n")
    .append("  HTTP Server:\n")
    .append("    Port: ").appendLine(server.port)
    .append("    Host: ").appendLine(server.host)
    .append("  Service:\n")
    .append("    Import Script:\n")
    .append("      Path: ").appendLine(service.importScript.path)
    .append("      Timeout: ").appendLine(service.importScript.maxSeconds.seconds)
    .append("    Install Meta Script:\n")
    .append("      Path: ").appendLine(service.installMetaScript.path)
    .append("      Timeout: ").appendLine(service.installMetaScript.maxSeconds.seconds)
    .append("    Install Data Script:\n")
    .append("      Path: ").appendLine(service.installDataScript.path)
    .append("      Timeout: ").appendLine(service.installDataScript.maxSeconds.seconds)
    .append("    Uninstall Script:\n")
    .append("      Path: ").appendLine(service.uninstallScript.path)
    .append("      Timeout: ").appendLine(service.uninstallScript.maxSeconds.seconds)
    .append("  Databases:\n")

  databases.forEach { (key, value) ->
    sb.append("    ").append(key).append(":\n")
      .append("      LDAP Query: ").appendLine(value.ldap)
      .append("      Username: ").appendLine(value.user)
      .append("      Password: ").appendLine(value.pass)
  }

  log.info(sb.toString())
}