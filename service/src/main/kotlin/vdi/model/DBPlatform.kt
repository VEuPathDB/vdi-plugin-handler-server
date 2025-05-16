package vdi.model


enum class DBPlatform(val value: String) {
  Oracle("Oracle"),
  Postgres("Postgresql");

  inline val defaultPort: UShort
    get() = when (this) {
      Oracle   -> 1521u
      Postgres -> 5432u
    }

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
