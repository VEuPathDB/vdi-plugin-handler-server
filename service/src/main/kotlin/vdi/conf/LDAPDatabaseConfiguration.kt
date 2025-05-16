package vdi.conf

import org.veupathdb.vdi.lib.common.field.SecretString

data class LDAPDatabaseConfiguration(
  override val name: String,
  override val user: String,
  override val pass: SecretString,
  val lookupCn: String,
): DatabaseConfiguration
