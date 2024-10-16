package vdi.server.context

import vdi.components.http.errors.BadRequestException
import vdi.components.ldap.LDAP
import vdi.conf.DatabaseConfiguration
import vdi.conf.DatabaseConfigurationMap
import vdi.model.DBPlatform
import vdi.model.DatabaseDetails

suspend fun withDatabaseDetails(
  databases: DatabaseConfigurationMap,
  ldap:      LDAP,
  projectID: String,
  fn:        suspend (dbDetails: DatabaseDetails) -> Unit,
) {
  fn((databases[projectID] ?: throw BadRequestException("unrecognized projectID value")).toDatabaseDetails(ldap))
}

private fun DatabaseConfiguration.toDatabaseDetails(ldap: LDAP?) =
  when (platform) {
    DBPlatform.Oracle -> this.ldap?.let { ldap!!.requireSingularOracleNetDesc(it) }
      ?.let { DatabaseDetails(it.host, it.port, it.serviceName, user, pass, dataSchema, platform) }
      ?: DatabaseDetails(host!!, port!!, dbName!!, user, pass, dataSchema, platform)
    DBPlatform.Postgres -> DatabaseDetails(host!!, port!!, dbName!!, user, pass, dataSchema, platform)
  }
