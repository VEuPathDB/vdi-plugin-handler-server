package vdi.server.controller

import io.ktor.server.application.*
import vdi.components.ldap.LDAP
import vdi.components.script.ScriptExecutor
import vdi.conf.HandlerConfig
import vdi.server.context.withDatabaseDetails
import vdi.server.context.withUninstallContext
import vdi.server.respond204
import vdi.service.UninstallHandler

suspend fun ApplicationCall.handleUninstallRequest(executor: ScriptExecutor, ldap: LDAP, config: HandlerConfig) {
  withUninstallContext { workspace, vdiID, projectID ->
    withDatabaseDetails(config.databases, ldap, projectID) { dbDetails ->
      UninstallHandler(workspace, vdiID, dbDetails, executor, config.service.uninstallScript).run()
      respond204()
    }
  }
}