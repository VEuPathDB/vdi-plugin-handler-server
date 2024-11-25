package vdi.server.context

import org.veupathdb.vdi.lib.common.field.ProjectID
import org.veupathdb.vdi.lib.common.model.VDIDatasetType
import vdi.components.http.errors.BadRequestException
import vdi.components.ldap.LDAP
import vdi.conf.DatabaseConfiguration
import vdi.conf.DatabaseConfigurationMap
import vdi.model.DBPlatform
import vdi.model.DatabaseDetails

suspend fun withDatabaseDetails(
  databases: DatabaseConfigurationMap,
  ldap:      LDAP,
  projectID: ProjectID,
  type:      VDIDatasetType,
  fn:        suspend (dbDetails: DatabaseDetails) -> Unit,
) =
  fn((databases[projectID to type.name]
    ?: throw BadRequestException("unrecognized projectID value")).toDatabaseDetails(ldap))

private fun DatabaseConfiguration.toDatabaseDetails(ldap: LDAP?) =
  when (platform) {
    DBPlatform.Oracle -> this.ldap?.let { ldap!!.requireSingularOracleNetDesc(it) }
      ?.let { DatabaseDetails(it.host, it.port, it.serviceName, user, pass, dataSchema, platform) }
      ?: DatabaseDetails(host!!, port!!, dbName!!, user, pass, dataSchema, platform)
    DBPlatform.Postgres -> DatabaseDetails(host!!, port!!, dbName!!, user, pass, dataSchema, platform)
  }
