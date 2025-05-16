package vdi.consts

import kotlin.time.Duration.Companion.hours

object ConfigDefault {

  const val ServerPort: UShort = 80u
  const val ServerHost = "0.0.0.0"

  const val ImportScriptPath = "/opt/veupathdb/bin/import"
  const val DataInstallScriptPath = "/opt/veupathdb/bin/install-data"
  const val MetaInstallScriptPath = "/opt/veupathdb/bin/install-meta"
  const val UninstallScriptPath = "/opt/veupathdb/bin/uninstall"
  const val CheckCompatScriptPath = "/opt/veupathdb/bin/check-compatibility"

  const val CustomPath = ""
  const val InstallRoot = "/datasets"

  inline val ImportScriptMaxDuration get() = 1.hours
  inline val DataInstallScriptMaxDuration get() = 1.hours
  inline val MetaInstallScriptMaxDuration get() = 1.hours
  inline val UninstallScriptMaxDuration get() = 1.hours
  inline val CheckCompatScriptMaxDuration get() = 1.hours
}
