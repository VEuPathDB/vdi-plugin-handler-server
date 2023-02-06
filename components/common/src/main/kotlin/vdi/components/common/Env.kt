package vdi.components.common

object Env {
  fun require(key: String) = System.getenv(key)
    .let { if (it.isNullOrBlank()) throw MissingRequiredEnvVarException(key) else it }
}