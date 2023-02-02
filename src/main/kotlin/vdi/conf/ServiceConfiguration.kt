package vdi.conf

import vdi.Const
import kotlin.time.Duration

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
  constructor(env: Map<String, String> = System.getenv()) : this(
    env[Const.EnvKey.ImportScriptPath] ?: Const.ConfigDefault.ImportScriptPath,
    (env[Const.EnvKey.ImportScriptMaxDuration] ?: Const.ConfigDefault.ImportScriptMaxDuration).toDurSeconds(),
    env[Const.EnvKey.DataInstallScriptPath] ?: Const.ConfigDefault.DataInstallScriptPath,
    (env[Const.EnvKey.DataInstallScriptMaxDuration] ?: Const.ConfigDefault.DataInstallScriptMaxDuration).toDurSeconds(),
    env[Const.EnvKey.MetaInstallScriptPath] ?: Const.ConfigDefault.MetaInstallScriptPath,
    (env[Const.EnvKey.MetaInstallScriptMaxDuration] ?: Const.ConfigDefault.MetaInstallScriptMaxDuration).toDurSeconds(),
    env[Const.EnvKey.UninstallScriptPath] ?: Const.ConfigDefault.UninstallScriptPath,
    (env[Const.EnvKey.UninstallScriptMaxDuration] ?: Const.ConfigDefault.UninstallScriptMaxDuration).toDurSeconds()
  )
}

@Suppress("NOTHING_TO_INLINE")
private inline fun String.toDurSeconds() = Duration.parse(this).inWholeSeconds
