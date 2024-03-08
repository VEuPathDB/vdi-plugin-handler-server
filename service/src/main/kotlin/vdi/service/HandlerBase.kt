package vdi.service

import org.slf4j.LoggerFactory
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
  private val log = LoggerFactory.getLogger(javaClass)

  init {
    log.trace("::init(workspace={}, executor={}, metrics={})", workspace, executor, metrics)
  }

  abstract suspend fun run(): T

  protected fun buildScriptEnv(): Environment {
    val out = System.getenv().toMutableMap() // Copy the environment before appending script env.

    if (customPath.isBlank())
      out["PATH"] = System.getenv("PATH")
    else
      out["PATH"] = System.getenv("PATH") + ':' + customPath

    out[ScriptEnvKey.DatasetID] = datasetID.toString()


    appendScriptEnv(out)

    return out
  }

  protected open fun appendScriptEnv(env: MutableMap<String, String>) {}
}
