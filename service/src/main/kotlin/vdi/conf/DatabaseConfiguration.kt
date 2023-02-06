package vdi.conf

import vdi.components.common.SecretString

/**
 * Database Configuration
 *
 * @author Elizabeth Paige Harper - https://github.com/foxcapades
 * @since 1.0.0
 */
data class DatabaseConfiguration(
  val name: String,
  val ldap: SecretString,
  val user: SecretString,
  val pass: SecretString,
)