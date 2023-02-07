package vdi.components.common

/**
 * Environment Variable Accessors
 */
object Env : EnvironmentAccessor {
  override fun require(key: String) =
    System.getenv(key)
      .let { if (it.isNullOrBlank()) throw MissingRequiredEnvVarException(key) else it }

  override fun optional(key: String): String? = System.getenv(key).blankToNull()

  override fun <T> require(key: String, fn: (String) -> T) = fn(require(key))

  override fun <T> optional(key: String, fn: (String) -> T) = optional(key)?.let(fn)

  override fun rawEnvironment() = System.getenv() as Map<String, String>
}