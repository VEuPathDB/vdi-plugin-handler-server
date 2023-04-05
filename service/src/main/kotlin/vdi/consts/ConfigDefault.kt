package vdi.consts

object ConfigDefault {

  const val ServerPort = "80"
  const val ServerHost = "0.0.0.0"

  const val ImportScriptPath = "/opt/veupathdb/bin/import"
  const val DataInstallScriptPath = "/opt/veupathdb/bin/install-data"
  const val MetaInstallScriptPath = "/opt/veupathdb/bin/install-meta"
  const val UninstallScriptPath = "/opt/veupathdb/bin/uninstall"
  const val CheckCompatScriptPath = "/opt/veupathdb/bin/check-compatibility"

  const val ImportScriptMaxDuration = "1h"
  const val DataInstallScriptMaxDuration = "1h"
  const val MetaInstallScriptMaxDuration = "1h"
  const val UninstallScriptMaxDuration = "1h"
  const val CheckCompatScriptMaxDuration = "1h"
}