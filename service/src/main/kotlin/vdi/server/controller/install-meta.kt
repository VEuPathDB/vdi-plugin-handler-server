package vdi.server.controller

import io.ktor.server.application.*
import vdi.components.http.errors.BadRequestException
import vdi.components.ldap.LDAP
import vdi.components.ldap.OracleNetDesc
import vdi.components.script.ScriptExecutor
import vdi.conf.DatabaseConfiguration
import vdi.conf.HandlerConfig
import vdi.server.context.withInstallMetaContext
import vdi.service.InstallMetaHandler


suspend fun ApplicationCall.handleInstallMetaRequest(config: HandlerConfig, ldap: LDAP, executor: ScriptExecutor) {
  withInstallMetaContext { workspace, request ->
    val dbDetails = (config.databases[request.projectID] ?: throw BadRequestException("unrecognized projectID value"))
      .let { it to ldap.requireSingularOracleNetDesc(it.ldap.value) }
      .toDatabaseDetails()

    InstallMetaHandler(workspace, request.vdiID, request.meta, dbDetails, executor, config.service.installMetaScript)
      .run()
  }
}

@Suppress("NOTHING_TO_INLINE")
private inline fun Pair<DatabaseConfiguration, OracleNetDesc>.toDatabaseDetails() =
  InstallMetaHandler.DatabaseDetails(second.host, second.port, second.serviceName, first.user, first.pass)