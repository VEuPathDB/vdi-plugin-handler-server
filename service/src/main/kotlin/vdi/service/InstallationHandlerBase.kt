package vdi.service

import org.veupathdb.vdi.lib.common.field.DatasetID
import vdi.components.metrics.ScriptMetrics
import vdi.components.script.ScriptExecutor
import vdi.consts.ScriptEnvKey
import java.nio.file.Path

sealed class InstallationHandlerBase<T>(
  datasetID: DatasetID,
  workspace: Path,
  executor: ScriptExecutor,
  customPath: String,

  /**
   * User dataset install path
   */
  protected val datasetInstallPath: Path,

  metrics: ScriptMetrics,
) : HandlerBase<T>(datasetID, workspace, executor, customPath, metrics) {

  override fun appendScriptEnv(env: MutableMap<String, String>) {
    env[ScriptEnvKey.InstallPath] = datasetInstallPath.toString()
  }
}