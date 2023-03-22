package vdi.model

import vdi.components.common.SecretString
import vdi.consts.ScriptEnvKey

data class DatabaseDetails(
  val dbHost: String,
  val dbPort: UShort,
  val dbName: String,
  val dbUser: SecretString,
  val dbPass: SecretString,
  val dbPlatform: DBPlatform,
) {
  fun toEnvMap(): Map<String, String> = mapOf(
    ScriptEnvKey.DBHost to dbHost,
    ScriptEnvKey.DBPort to dbPort.toString(),
    ScriptEnvKey.DBName to dbName,
    ScriptEnvKey.DBUser to dbUser.value,
    ScriptEnvKey.DBPass to dbPass.value,
    ScriptEnvKey.DBPlatform to dbPlatform.value,
  )
}
