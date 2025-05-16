package vdi.conf

import org.veupathdb.vdi.lib.common.field.SecretString
import org.veupathdb.vdi.lib.common.util.HostAddress
import vdi.model.DBPlatform

data class DirectDatabaseConfiguration(
  override val name: String,
  override val user: String,
  override val pass: SecretString,
  val dbName: String,
  val server: HostAddress,
  val platform: DBPlatform,
): DatabaseConfiguration
