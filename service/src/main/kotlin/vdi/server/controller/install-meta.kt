package vdi.server.controller

import io.ktor.server.application.*
import vdi.components.ldap.LDAP
import vdi.components.script.ScriptExecutor
import vdi.conf.HandlerConfig
import vdi.server.context.withDatabaseDetails
import vdi.server.context.withInstallMetaContext
import vdi.server.respond204
import vdi.service.InstallMetaHandler

suspend fun ApplicationCall.handleInstallMetaRequest(config: HandlerConfig, ldap: LDAP, executor: ScriptExecutor) {
  withInstallMetaContext { workspace, request ->
    withDatabaseDetails(config.databases, ldap, request.projectID) { dbDetails ->
      InstallMetaHandler(workspace, request.vdiID, request.meta, dbDetails, executor, config.service.installMetaScript)
        .run()

      respond204()
    }
  }
}
