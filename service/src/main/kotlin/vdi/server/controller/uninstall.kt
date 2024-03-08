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
        workspace   = workspace,
        datasetID   = vdiID,
        dbDetails   = dbDetails,
        executor    = appCtx.executor,
        customPath  = appCtx.config.service.customPath,
        installPath = appCtx.pathFactory.makePath(projectID, vdiID.toString()),
        script      = appCtx.config.service.uninstallScript,
        metrics     = appCtx.metrics.scriptMetrics,
      )
        .run()
      respond204()
    }
  }
}