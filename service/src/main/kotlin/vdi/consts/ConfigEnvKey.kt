package vdi.consts

/**
 * Service Configuration Environment Key
 */
object ConfigEnvKey {

  const val LDAPServer = "LDAP_SERVER"
  const val OracleBaseDN = "ORACLE_BASE_DN"

  const val ImportScriptPath = "IMPORT_SCRIPT_PATH"
  const val ImportScriptMaxDuration = "IMPORT_SCRIPT_MAX_DURATION"

  const val DataInstallScriptPath = "INSTALL_DATA_SCRIPT_PATH"
  const val DataInstallScriptMaxDuration = "INSTALL_DATA_SCRIPT_MAX_DURATION"

  const val MetaInstallScriptPath = "INSTALL_META_SCRIPT_PATH"
  const val MetaInstallScriptMaxDuration = "INSTALL_META_SCRIPT_MAX_DURATION"

  const val UninstallScriptPath = "UNINSTALL_SCRIPT_PATH"
  const val UninstallScriptMaxDuration = "UNINSTALL_SCRIPT_MAX_DURATION"

  const val CheckCompatScriptPath = "CHECK_COMPAT_SCRIPT_PATH"
  const val CheckCompatScriptMaxDuration = "CHECK_COMPAT_SCRIPT_MAX_DURATION"

  const val ServerPort = "SERVER_PORT"
  const val ServerHost = "SERVER_HOST"
}