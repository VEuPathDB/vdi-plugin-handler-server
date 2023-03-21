package vdi.server.controller

import io.ktor.server.application.*
import vdi.model.ApplicationContext
import vdi.server.context.withDatabaseDetails
import vdi.server.context.withInstallDataContext
import vdi.server.model.InstallDataSuccessResponse
import vdi.server.model.WarningsListResponse
import vdi.server.respondJSON200
import vdi.server.respondJSON418
import vdi.service.InstallDataHandler

suspend fun ApplicationCall.handleInstallDataRequest(appCtx: ApplicationContext) {
  withInstallDataContext { workspace, details, payload ->
    withDatabaseDetails(appCtx.config.databases, appCtx.ldap, details.projectID) { dbDetails ->
      try {
        // Run the install-data service and collect the returned list of
        // installation warnings.
        val warnings = InstallDataHandler(
          workspace,
          details.vdiID,
          payload,
          dbDetails,
          appCtx.executor,
          appCtx.config.service.installMetaScript,
          appCtx.config.service.installDataScript,
          appCtx.metrics.scriptMetrics,
        )
          .run()

        respondJSON200(InstallDataSuccessResponse(warnings))
      } catch (e: InstallDataHandler.ValidationError) {
        respondJSON418(WarningsListResponse(e.warnings))
      }
    }
  }
}