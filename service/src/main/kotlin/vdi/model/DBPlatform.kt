package vdi.model


enum class DBPlatform(val value: String) {
  Oracle("Oracle"),
  Postgres("Postgresql");

  companion object {
    fun fromPlatformString(platformString: String?): DBPlatform {
      return DBPlatform.entries
        .find { platformString.equals(other = it.value, ignoreCase = true) } ?: Oracle // Default to Oracle
    }
  }
}
