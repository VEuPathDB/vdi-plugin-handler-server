package vdi.server.controller

import io.ktor.server.application.*
import vdi.model.ApplicationContext
import vdi.server.context.withDatabaseDetails
import vdi.server.context.withInstallMetaContext
import vdi.server.respond204
import vdi.service.InstallMetaHandler

suspend fun ApplicationCall.handleInstallMetaRequest(appCtx: ApplicationContext) {
  withInstallMetaContext { workspace, request ->
    withDatabaseDetails(appCtx.config.databases, appCtx.ldap, request.projectID) { dbDetails ->
      InstallMetaHandler(
        workspace,
        request.vdiID,
        request.meta,
        dbDetails,
        appCtx.executor,
        appCtx.config.service.installMetaScript,
        appCtx.metrics.scriptMetrics,
      )
        .run()

      respond204()
    }
  }
}
