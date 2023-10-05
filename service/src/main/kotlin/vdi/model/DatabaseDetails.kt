package vdi.model

import org.veupathdb.vdi.lib.common.field.SecretString
import vdi.consts.ScriptEnvKey

data class DatabaseDetails(
  val dbHost: String,
  val dbPort: UShort,
  val dbName: String,
  val dbUser: String,
  val dbPass: SecretString,
  val dbSchema: String,
  val dbPlatform: DBPlatform,
) {
  fun toEnvMap(): Map<String, String> = mapOf(
    ScriptEnvKey.DBHost to dbHost,
    ScriptEnvKey.DBPort to dbPort.toString(),
    ScriptEnvKey.DBName to dbName,
    ScriptEnvKey.DBUser to dbUser,
    ScriptEnvKey.DBPass to dbPass.unwrap(),
    ScriptEnvKey.DBSchema to dbSchema,
    ScriptEnvKey.DBPlatform to dbPlatform.value,
  )
}
