package vdi.conf

import org.veupathdb.vdi.lib.common.field.SecretString
import vdi.model.DBPlatform

/**
 * Database Configuration
 *
 * @author Elizabeth Paige Harper - https://github.com/foxcapades
 * @since 1.0.0
 */
data class DatabaseConfiguration(
  val connectionName: String,
  val ldap: String?,
  val user: String,
  val pass: SecretString,
  val dataSchema: String,
  val platform: DBPlatform,
  val host: String?,
  val port: UShort?,
  val dbName: String?
)
