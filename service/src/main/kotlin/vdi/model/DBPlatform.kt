package vdi.model


enum class DBPlatform(val value: String) {
  Oracle("Oracle"),
  Postgres("Postgresql");

  companion object {
    @JvmStatic
    fun fromPlatformString(value: String) =
      when (value.lowercase()) {
        "oracle"                 -> Oracle
        "postgres", "postgresql" -> Postgres
        else                     -> throw IllegalArgumentException("unrecognized DBPlatform value: $value")
      }
  }
}
