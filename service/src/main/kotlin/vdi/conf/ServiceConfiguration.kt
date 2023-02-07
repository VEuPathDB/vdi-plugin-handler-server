package vdi.conf

import vdi.Const
import kotlin.time.Duration
import vdi.components.common.EnvironmentAccessor

/**
 * Service Specific Configuration Options
 *
 * @author Elizabeth Paige Harper - https://github.com/foxcapades
 * @since 1.0.0
 */
data class ServiceConfiguration(
  val ldapServer: String,
  val oracleBaseDN: String,

  val importScriptPath: String,
  val importScriptMaxSeconds: Long,

  val installDataScriptPath: String,
  val installDataScriptMaxSeconds: Long,

  val installMetaScriptPath: String,
  val installMetaScriptMaxSeconds: Long,

  val uninstallScriptPath: String,
  val uninstallScriptMaxSeconds: Long,
) {
  constructor(env: EnvironmentAccessor) : this(
    env.require(Const.ConfigEnvKey.LDAPServer),
    env.require(Const.ConfigEnvKey.OracleBaseDN),
    env.optional(Const.ConfigEnvKey.ImportScriptPath) ?: Const.ConfigDefault.ImportScriptPath,
    (env.optional(Const.ConfigEnvKey.ImportScriptMaxDuration) ?: Const.ConfigDefault.ImportScriptMaxDuration).toDurSeconds(),
    env.optional(Const.ConfigEnvKey.DataInstallScriptPath) ?: Const.ConfigDefault.DataInstallScriptPath,
    (env.optional(Const.ConfigEnvKey.DataInstallScriptMaxDuration) ?: Const.ConfigDefault.DataInstallScriptMaxDuration).toDurSeconds(),
    env.optional(Const.ConfigEnvKey.MetaInstallScriptPath) ?: Const.ConfigDefault.MetaInstallScriptPath,
    (env.optional(Const.ConfigEnvKey.MetaInstallScriptMaxDuration) ?: Const.ConfigDefault.MetaInstallScriptMaxDuration).toDurSeconds(),
    env.optional(Const.ConfigEnvKey.UninstallScriptPath) ?: Const.ConfigDefault.UninstallScriptPath,
    (env.optional(Const.ConfigEnvKey.UninstallScriptMaxDuration) ?: Const.ConfigDefault.UninstallScriptMaxDuration).toDurSeconds()
  )
}

@Suppress("NOTHING_TO_INLINE")
private inline fun String.toDurSeconds() = Duration.parse(this).inWholeSeconds
