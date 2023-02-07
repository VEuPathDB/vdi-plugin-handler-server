package vdi.consts

object ConfigDefault {

  const val ServerPort = "80"
  const val ServerHost = "0.0.0.0"

  const val ImportScriptPath = "/opt/veupathdb/import"
  const val DataInstallScriptPath = "/opt/veupathdb/install-data"
  const val MetaInstallScriptPath = "/opt/veupathdb/install-meta"
  const val UninstallScriptPath = "/opt/veupathdb/uninstall"

  const val ImportScriptMaxDuration = "1h"
  const val DataInstallScriptMaxDuration = "1h"
  const val MetaInstallScriptMaxDuration = "1h"
  const val UninstallScriptMaxDuration = "1h"
}