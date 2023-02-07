package vdi.server.controller

import io.ktor.server.application.*
import vdi.components.http.errors.BadRequestException
import vdi.components.ldap.LDAP
import vdi.components.script.ScriptExecutor
import vdi.conf.Configuration
import vdi.server.context.withInstallDataContext
import vdi.server.model.InstallDataSuccessResponse
import vdi.server.respondJSON200
import vdi.service.InstallDataHandler

suspend fun ApplicationCall.handleInstallDataRequest(
  config:   Configuration,
  ldap:     LDAP,
  executor: ScriptExecutor,
) {
  withInstallDataContext { workspace, details, payload ->
    // Lookup the database configuration from the environment, failing if no
    // such entry exists.
    val dbEnvConfig = config.databases[details.projectID] ?: throw BadRequestException("unrecognized projectID value")

    // Run the install-data service and collect the returned list of
    // installation warnings.
    val warnings = InstallDataHandler(workspace, details.vdiID, payload, ldap, executor, dbEnvConfig, config.service.installDataScript)
      .run()

    respondJSON200(InstallDataSuccessResponse(warnings))
  }
}