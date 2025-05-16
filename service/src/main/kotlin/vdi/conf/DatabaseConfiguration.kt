package vdi.conf

import org.veupathdb.vdi.lib.common.field.SecretString

sealed interface DatabaseConfiguration {
  val name: String
  val user: String
  val pass: SecretString
}

