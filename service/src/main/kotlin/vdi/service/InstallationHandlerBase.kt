package vdi.service

import vdi.components.metrics.ScriptMetrics
import vdi.components.script.ScriptExecutor
import vdi.consts.ScriptEnvKey
import java.nio.file.Path

sealed class InstallationHandlerBase<T>(
  workspace: Path,
  executor: ScriptExecutor,
  customPath: String,

  /**
   * User dataset install path
   */
  protected val datasetInstallPath: Path,

  metrics: ScriptMetrics,
) : HandlerBase<T>(workspace, executor, customPath, metrics) {

  override fun appendScriptEnv(env: MutableMap<String, String>) {
    env[ScriptEnvKey.InstallPath] = datasetInstallPath.toString()
  }
}