package vdi.conf

import org.slf4j.Logger
import kotlin.time.Duration.Companion.seconds

fun Configuration.printToLogs(log: Logger) {
  val sb = StringBuilder(4096)

  sb.append("Configuration:\n")
    .append("  HTTP Server:\n")
    .append("    Port: ").appendLine(ServerConfiguration.port)
    .append("    Host: ").appendLine(ServerConfiguration.host)
    .append("  Service:\n")
    .append("    Import Script:\n")
    .append("      Path: ").appendLine(ServiceConfiguration.importScriptPath)
    .append("      Timeout: ").appendLine(ServiceConfiguration.importScriptMaxSeconds.seconds)
    .append("    Install Meta Script:\n")
    .append("      Path: ").appendLine(ServiceConfiguration.installMetaScriptPath)
    .append("      Timeout: ").appendLine(ServiceConfiguration.installMetaScriptMaxSeconds.seconds)
    .append("    Install Data Script:\n")
    .append("      Path: ").appendLine(ServiceConfiguration.installDataScriptPath)
    .append("      Timeout: ").appendLine(ServiceConfiguration.installDataScriptMaxSeconds.seconds)
    .append("    Uninstall Script:\n")
    .append("      Path: ").appendLine(ServiceConfiguration.uninstallScriptPath)
    .append("      Timeout: ").appendLine(ServiceConfiguration.uninstallScriptMaxSeconds.seconds)
    .append("  Databases:\n")

  DatabaseConfigurations.forEach { (key, value) ->
    sb.append("    ").append(key).append(":\n")
      .append("      LDAP Query: ").appendLine(value.ldap)
      .append("      Username: ").appendLine(value.user)
      .append("      Password: ").appendLine(value.pass)
  }

  log.info(sb.toString())
}