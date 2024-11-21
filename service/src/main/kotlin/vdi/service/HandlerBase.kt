package vdi.service

import org.veupathdb.vdi.lib.common.env.EnvKey
import org.veupathdb.vdi.lib.common.env.Environment
import org.veupathdb.vdi.lib.common.field.DatasetID
import java.nio.file.Path
import vdi.components.metrics.ScriptMetrics
import vdi.components.script.ScriptExecutor
import vdi.consts.ScriptEnvKey

sealed class HandlerBase<T>(
  /**
   * VDI ID of the dataset to which the data being processed belongs.
   */
  protected val datasetID: DatasetID,

  /**
   * Path to the workspace for the script execution.
   */
  protected val workspace: Path,

  /**
   * Script executor service.
   */
  protected val executor: ScriptExecutor,

  /**
   * Custom PATH environment variable entries
   */
  protected val customPath: String,

  /**
   * Metrics collector.
   */
  protected val metrics: ScriptMetrics,
) {
  abstract suspend fun run(): T

  protected fun buildScriptEnv(): Environment {
    val out = System.getenv()
      .asSequence()
      .filter { (k, _) -> !k.startsWith(EnvKey.AppDB.CommonPrefix) }
      .associateByTo(HashMap<String, String>(), Map.Entry<String, String>::key, Map.Entry<String, String>::value)

    if (customPath.isNotBlank())
      out["PATH"] = out["PATH"] + ':' + customPath

    out[ScriptEnvKey.DatasetID] = datasetID.toString()

    appendScriptEnv(out)

    return out
  }

  protected open fun appendScriptEnv(env: MutableMap<String, String>) {}
}
