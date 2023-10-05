package vdi.conf

import org.veupathdb.vdi.lib.common.env.Environment
import org.veupathdb.vdi.lib.common.env.optional
import org.veupathdb.vdi.lib.common.env.require
import kotlin.time.Duration
import vdi.consts.ConfigDefault
import vdi.consts.ConfigEnvKey

/**
 * Service Specific Configuration Options
 *
 * @author Elizabeth Paige Harper - https://github.com/foxcapades
 * @since 1.0.0
 */
data class ServiceConfiguration(
  val ldapServer: String,
  val oracleBaseDN: String,

  val importScript: ScriptConfiguration,
  val installDataScript: ScriptConfiguration,
  val installMetaScript: ScriptConfiguration,
  val uninstallScript: ScriptConfiguration,
  val checkCompatScript: ScriptConfiguration,

  val customPath: String,
  val datasetRoot: String,
  val siteBuild: String,
) {
  constructor(env: Environment) : this(
    env.require(ConfigEnvKey.LDAPServer),
    env.require(ConfigEnvKey.OracleBaseDN),
    ScriptConfiguration(
      env.optional(ConfigEnvKey.ImportScriptPath) ?: ConfigDefault.ImportScriptPath,
      (env.optional(ConfigEnvKey.ImportScriptMaxDuration) ?: ConfigDefault.ImportScriptMaxDuration).toDurSeconds()
    ),
    ScriptConfiguration(
      env.optional(ConfigEnvKey.DataInstallScriptPath) ?: ConfigDefault.DataInstallScriptPath,
      (env.optional(ConfigEnvKey.DataInstallScriptMaxDuration) ?: ConfigDefault.DataInstallScriptMaxDuration).toDurSeconds(),
    ),
    ScriptConfiguration(
      env.optional(ConfigEnvKey.MetaInstallScriptPath) ?: ConfigDefault.MetaInstallScriptPath,
      (env.optional(ConfigEnvKey.MetaInstallScriptMaxDuration) ?: ConfigDefault.MetaInstallScriptMaxDuration).toDurSeconds(),
    ),
    ScriptConfiguration(
      env.optional(ConfigEnvKey.UninstallScriptPath) ?: ConfigDefault.UninstallScriptPath,
      (env.optional(ConfigEnvKey.UninstallScriptMaxDuration) ?: ConfigDefault.UninstallScriptMaxDuration).toDurSeconds(),
    ),
    ScriptConfiguration(
      env.optional(ConfigEnvKey.CheckCompatScriptPath) ?: ConfigDefault.CheckCompatScriptPath,
      (env.optional(ConfigEnvKey.CheckCompatScriptMaxDuration) ?: ConfigDefault.CheckCompatScriptMaxDuration).toDurSeconds(),
    ),
    env.optional(ConfigEnvKey.CustomPath) ?: ConfigDefault.CustomPath,
    env.require(ConfigEnvKey.DatasetInstallRoot),
    env.require(ConfigEnvKey.SiteBuild),
  )
}

@Suppress("NOTHING_TO_INLINE")
private inline fun String.toDurSeconds() = Duration.parse(this).inWholeSeconds
