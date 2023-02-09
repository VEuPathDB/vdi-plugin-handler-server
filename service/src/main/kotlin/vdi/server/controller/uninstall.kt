package vdi.server.controller

import io.ktor.server.application.*
import vdi.model.ApplicationContext
import vdi.server.context.withDatabaseDetails
import vdi.server.context.withUninstallContext
import vdi.server.respond204
import vdi.service.UninstallHandler

suspend fun ApplicationCall.handleUninstallRequest(appCtx: ApplicationContext) {
  withUninstallContext { workspace, vdiID, projectID ->
    withDatabaseDetails(appCtx.config.databases, appCtx.ldap, projectID) { dbDetails ->
      UninstallHandler(
        workspace,
        vdiID,
        dbDetails,
        appCtx.executor,
        appCtx.config.service.uninstallScript,
        appCtx.metrics.scriptMetrics,
      )
        .run()
      respond204()
    }
  }
}