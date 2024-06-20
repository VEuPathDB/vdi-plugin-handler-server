package vdi.model

import org.veupathdb.vdi.lib.common.util.or

enum class DBPlatform(val value: String) {
  Oracle("Oracle"),
  Postgres("Postgresql");

  companion object {
    fun fromPlatformString(platformString: String?): DBPlatform {
      return values().asSequence()
        .find { platformString.equals(other = it.value, ignoreCase = true) }
        .or { Oracle } // Default to Oracle
    }
  }
}