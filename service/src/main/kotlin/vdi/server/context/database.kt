package vdi.server.context

import vdi.components.http.errors.BadRequestException
import vdi.components.ldap.LDAP
import vdi.components.ldap.OracleNetDesc
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

private fun DatabaseConfiguration.toDatabaseDetails(ldap: LDAP?): DatabaseDetails {
  return when (val dbPlatform = DBPlatform.fromPlatformString(platform)) {
    DBPlatform.Oracle -> {
      val lookupDetails = ldap!!.requireSingularOracleNetDesc(this.ldap!!)
      return DatabaseDetails(lookupDetails.host, lookupDetails.port, lookupDetails.serviceName, user, pass, dataSchema, dbPlatform)
    }
    DBPlatform.Postgres -> DatabaseDetails(host!!, port!!, pgName!!, user, pass, dataSchema, dbPlatform)
  }
}
