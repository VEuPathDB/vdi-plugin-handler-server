package vdi.server.controller

import io.ktor.server.application.*
import org.veupathdb.vdi.lib.common.intra.SimpleErrorResponse
import org.veupathdb.vdi.lib.common.intra.WarningResponse
import vdi.model.ApplicationContext
import vdi.server.context.withDatabaseDetails
import vdi.server.context.withInstallDataContext
import vdi.server.respondJSON200
import vdi.server.respondJSON418
import vdi.server.respondJSON420
import vdi.server.respondJSON500
import vdi.service.InstallDataHandler

suspend fun ApplicationCall.handleInstallDataRequest(appCtx: ApplicationContext) {
  withInstallDataContext { workspace, request, payload ->
    withDatabaseDetails(appCtx.config.databases, appCtx.ldap, request.projectID) { dbDetails ->
      try {
        // Run the install-data service and collect the returned list of
        // installation warnings.
        val warnings = InstallDataHandler(
          workspace          = workspace,
          request            = request,
          payload            = payload,
          dbDetails          = dbDetails,
          executor           = appCtx.executor,
          customPath         = appCtx.config.service.customPath,
          datasetInstallPath = appCtx.pathFactory.makePath(request.projectID, request.vdiID),
          metaScript         = appCtx.config.service.installMetaScript,
          dataScript         = appCtx.config.service.installDataScript,
          compatScript       = appCtx.config.service.checkCompatScript,
          metrics            = appCtx.metrics.scriptMetrics,
        )
          .run()

        respondJSON200(WarningResponse(warnings))
      } catch (e: InstallDataHandler.ValidationError) {
        respondJSON418(WarningResponse(e.warnings))
      } catch (e: InstallDataHandler.CompatibilityError) {
        respondJSON420(WarningResponse(e.warnings))
      } catch (e: InstallDataHandler.InstallDirConflictError) {
        respondJSON500(SimpleErrorResponse(e.message!!))
      }
    }
  }
}