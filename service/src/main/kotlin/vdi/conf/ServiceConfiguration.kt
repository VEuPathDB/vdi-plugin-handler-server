package vdi.conf

import vdi.consts.ConfigDefault

/**
 * Service Specific Configuration Options
 *
 * @author Elizabeth Paige Harper - https://github.com/foxcapades
 * @since 1.0.0
 */
data class ServiceConfiguration(
  val importScript: ScriptConfiguration,
  val installDataScript: ScriptConfiguration,
  val installMetaScript: ScriptConfiguration,
  val uninstallScript: ScriptConfiguration,
  val checkCompatScript: ScriptConfiguration,

  val customPath: String,
  val datasetRoot: String,
  val siteBuild: String,
) {
  constructor(raw: RawServiceConfiguration): this(
    importScript = raw.scripts?.dataCleaning.let { ScriptConfiguration(
      it?.pathOverride ?: ConfigDefault.ImportScriptPath,
      it?.maxDuration ?: ConfigDefault.ImportScriptMaxDuration,
    ) },
    installDataScript = raw.scripts?.dataInstall.let { ScriptConfiguration(
      it?.pathOverride ?: ConfigDefault.DataInstallScriptPath,
      it?.maxDuration ?: ConfigDefault.DataInstallScriptMaxDuration,
    ) },
    installMetaScript = raw.scripts?.metaUpdate.let { ScriptConfiguration(
      it?.pathOverride ?: ConfigDefault.MetaInstallScriptPath,
      it?.maxDuration ?: ConfigDefault.MetaInstallScriptMaxDuration,
    ) },
    uninstallScript = raw.scripts?.uninstall.let { ScriptConfiguration(
      it?.pathOverride ?: ConfigDefault.UninstallScriptPath,
      it?.maxDuration ?: ConfigDefault.UninstallScriptMaxDuration
    ) },
    checkCompatScript = raw.scripts?.checkCompatibility.let { ScriptConfiguration(
      it?.pathOverride ?: ConfigDefault.CheckCompatScriptPath,
      it?.maxDuration ?: ConfigDefault.CheckCompatScriptMaxDuration
    ) },
    customPath = raw.customPath ?: ConfigDefault.CustomPath,
    datasetRoot = raw.installRoot ?: ConfigDefault.InstallRoot,
    siteBuild = raw.siteBuild,
  )
}
