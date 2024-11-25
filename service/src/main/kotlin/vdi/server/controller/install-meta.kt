package vdi.server.controller

import io.ktor.server.application.*
import vdi.model.ApplicationContext
import vdi.server.context.withDatabaseDetails
import vdi.server.context.withInstallMetaContext
import vdi.server.respond204
import vdi.service.InstallMetaHandler

suspend fun ApplicationCall.handleInstallMetaRequest(appCtx: ApplicationContext) {
  withInstallMetaContext { workspace, request ->
    withDatabaseDetails(appCtx.config.databases, appCtx.ldap, request.projectID, request.meta.type) { dbDetails ->
      InstallMetaHandler(
        workspace   = workspace,
        request     = request,
        dbDetails   = dbDetails,
        executor    = appCtx.executor,
        customPath  = appCtx.config.service.customPath,
        installPath = appCtx.pathFactory.makePath(request.projectID, request.vdiID),
        script      = appCtx.config.service.installMetaScript,
        metrics     = appCtx.metrics.scriptMetrics,
      )
        .run()

      respond204()
    }
  }
}
