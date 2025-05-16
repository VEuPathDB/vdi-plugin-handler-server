package vdi.server.context

import org.veupathdb.lib.ldap.LDAP
import org.veupathdb.vdi.lib.common.field.ProjectID
import org.veupathdb.vdi.lib.common.model.VDIDatasetType
import vdi.components.http.errors.BadRequestException
import vdi.conf.DatabaseConfiguration
import vdi.conf.DatabaseConfigurationMap
import vdi.conf.DirectDatabaseConfiguration
import vdi.conf.LDAPDatabaseConfiguration
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

private fun DatabaseConfiguration.toDatabaseDetails(ldap: LDAP) =
  when (this) {
    is DirectDatabaseConfiguration -> DatabaseDetails(
      server.host,
      server.port,
      dbName,
      user,
      pass,
      user,
      platform,
    )
    is LDAPDatabaseConfiguration   -> ldap.requireSingularNetDesc(lookupCn)
      .let { DatabaseDetails(it.host, it.port.toUShort(), it.identifier, user, pass, user, DBPlatform.fromPlatformString(it.platform.name)) }
  }
