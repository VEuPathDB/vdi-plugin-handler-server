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
  fn((databases[projectID] ?: throw BadRequestException("unrecognized projectID value"))
    .let { it to ldap.requireSingularOracleNetDesc(it.ldap) }
    .toDatabaseDetails())
}

// FIXME:
//   | Right now the db platform value is hardcoded to oracle.  When we
//   | (VEuPathDB) figure out how we will handle database connection detail
//   | lookups for Postgres, we will need to make the determination here or
//   | upstream of here what db platform we are actually using.
@Suppress("NOTHING_TO_INLINE")
private inline fun Pair<DatabaseConfiguration, OracleNetDesc>.toDatabaseDetails() =
  DatabaseDetails(second.host, second.port, second.serviceName, first.user, first.pass, first.schema, DBPlatform.Oracle)