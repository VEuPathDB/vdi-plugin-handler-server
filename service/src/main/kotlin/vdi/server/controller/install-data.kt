package vdi.server.controller

import io.ktor.server.application.*
import vdi.components.http.errors.BadRequestException
import vdi.components.ldap.LDAP
import vdi.components.script.ScriptExecutor
import vdi.conf.HandlerConfig
import vdi.server.context.withDatabaseDetails
import vdi.server.context.withInstallDataContext
import vdi.server.model.InstallDataSuccessResponse
import vdi.server.respondJSON200
import vdi.service.InstallDataHandler

suspend fun ApplicationCall.handleInstallDataRequest(
  config:   HandlerConfig,
  ldap:     LDAP,
  executor: ScriptExecutor,
) {
  withInstallDataContext { workspace, details, payload ->
    withDatabaseDetails(config.databases, ldap, details.projectID) { dbDetails ->
      // Run the install-data service and collect the returned list of
      // installation warnings.
      val warnings = InstallDataHandler(workspace, details.vdiID, payload, dbDetails, executor, config.service.installDataScript)
        .run()

      respondJSON200(InstallDataSuccessResponse(warnings))
    }
  }
}