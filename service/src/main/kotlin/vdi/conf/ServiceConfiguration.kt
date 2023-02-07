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
    env.optional(Const.EnvKey.ImportScriptPath) ?: Const.ConfigDefault.ImportScriptPath,
    (env.optional(Const.EnvKey.ImportScriptMaxDuration) ?: Const.ConfigDefault.ImportScriptMaxDuration).toDurSeconds(),
    env.optional(Const.EnvKey.DataInstallScriptPath) ?: Const.ConfigDefault.DataInstallScriptPath,
    (env.optional(Const.EnvKey.DataInstallScriptMaxDuration) ?: Const.ConfigDefault.DataInstallScriptMaxDuration).toDurSeconds(),
    env.optional(Const.EnvKey.MetaInstallScriptPath) ?: Const.ConfigDefault.MetaInstallScriptPath,
    (env.optional(Const.EnvKey.MetaInstallScriptMaxDuration) ?: Const.ConfigDefault.MetaInstallScriptMaxDuration).toDurSeconds(),
    env.optional(Const.EnvKey.UninstallScriptPath) ?: Const.ConfigDefault.UninstallScriptPath,
    (env.optional(Const.EnvKey.UninstallScriptMaxDuration) ?: Const.ConfigDefault.UninstallScriptMaxDuration).toDurSeconds()
  )
}

@Suppress("NOTHING_TO_INLINE")
private inline fun String.toDurSeconds() = Duration.parse(this).inWholeSeconds
