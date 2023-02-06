package vdi.components.common

open class StartupException : Throwable {
  constructor() : super()
  constructor(message: String) : super(message)
  constructor(cause: Throwable) : super(cause)
  constructor(message: String, cause: Throwable) : super(message, cause)
}

class MissingRequiredEnvVarException(envVarName: String)
  : StartupException("Required environment variable $envVarName was blank or unset.")