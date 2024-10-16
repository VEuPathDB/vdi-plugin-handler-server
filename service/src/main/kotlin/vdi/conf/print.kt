package vdi.conf

import org.slf4j.Logger
import kotlin.time.Duration.Companion.seconds

fun HandlerConfig.printToLogs(log: Logger) {
  val sb = StringBuilder(4096)

  sb.appendLine("Configuration:")
    .appendLine("  HTTP Server:")
    .append("    Port: ").appendLine(server.port)
    .append("    Host: ").appendLine(server.host)
    .appendLine("  Service:")
    .appendLine("    Import Script:")
    .append("      Path: ").appendLine(service.importScript.path)
    .append("      Timeout: ").appendLine(service.importScript.maxSeconds.seconds)
    .appendLine("    Install Meta Script:")
    .append("      Path: ").appendLine(service.installMetaScript.path)
    .append("      Timeout: ").appendLine(service.installMetaScript.maxSeconds.seconds)
    .appendLine("    Install Data Script:")
    .append("      Path: ").appendLine(service.installDataScript.path)
    .append("      Timeout: ").appendLine(service.installDataScript.maxSeconds.seconds)
    .appendLine("    Uninstall Script:")
    .append("      Path: ").appendLine(service.uninstallScript.path)
    .append("      Timeout: ").appendLine(service.uninstallScript.maxSeconds.seconds)
    .appendLine("  Databases:")

  databases.forEach { (key, value) ->
    sb.append("    ").append(key).appendLine(":")
      .append("      Connection: ").appendLine(value.connectionName)
      .append("      Data Schema: ").appendLine(value.dataSchema)
      .append("      Platform: ").appendLine(value.platform)

    if (value.ldap == null) {
      sb.append("      Host: ").appendLine(value.host)
        .append("      Port: ").appendLine(value.port)
        .append("      DB Name: ").appendLine(value.dbName)
    } else {
      sb.append("      LDAP: ").appendLine(value.ldap)
    }
  }

  log.info(sb.toString())
}
