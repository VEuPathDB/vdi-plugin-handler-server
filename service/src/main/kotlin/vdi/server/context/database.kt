package vdi.server.context

import vdi.components.http.errors.BadRequestException
import vdi.components.ldap.LDAP
import vdi.components.ldap.OracleNetDesc
import vdi.conf.DatabaseConfiguration
import vdi.conf.DatabaseConfigurationMap
import vdi.model.DatabaseDetails

suspend fun withDatabaseDetails(
  databases: DatabaseConfigurationMap,
  ldap:      LDAP,
  projectID: String,
  fn:        suspend (dbDetails: DatabaseDetails) -> Unit,
) {
  fn((databases[projectID] ?: throw BadRequestException("unrecognized projectID value"))
    .let { it to ldap.requireSingularOracleNetDesc(it.ldap.value) }
    .toDatabaseDetails())
}

@Suppress("NOTHING_TO_INLINE")
private inline fun Pair<DatabaseConfiguration, OracleNetDesc>.toDatabaseDetails() =
  DatabaseDetails(second.host, second.port, second.serviceName, first.user, first.pass)