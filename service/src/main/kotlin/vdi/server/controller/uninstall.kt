package vdi.server.controller

import io.ktor.server.application.*
import vdi.model.ApplicationContext
import vdi.server.context.withDatabaseDetails
import vdi.server.context.withUninstallContext
import vdi.server.respond204
import vdi.service.UninstallHandler

suspend fun ApplicationCall.handleUninstallRequest(appCtx: ApplicationContext) {
  withUninstallContext { workspace, request ->
    withDatabaseDetails(appCtx.config.databases, appCtx.ldap, request.projectID, request.type) { dbDetails ->
      UninstallHandler(
        workspace   = workspace,
        request     = request,
        dbDetails   = dbDetails,
        executor    = appCtx.executor,
        customPath  = appCtx.config.service.customPath,
        installPath = appCtx.pathFactory.makePath(request.projectID, request.vdiID),
        script      = appCtx.config.service.uninstallScript,
        metrics     = appCtx.metrics.scriptMetrics,
      )
        .run()
      respond204()
    }
  }
}
