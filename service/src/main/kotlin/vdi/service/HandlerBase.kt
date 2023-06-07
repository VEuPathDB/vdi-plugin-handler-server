package vdi.service

import org.slf4j.LoggerFactory
import org.veupathdb.vdi.lib.common.env.Environment
import java.nio.file.Path
import vdi.components.metrics.ScriptMetrics
import vdi.components.script.ScriptExecutor
import java.util.Map.copyOf

sealed class HandlerBase<T>(
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

    appendScriptEnv(out)

    return out
  }

  protected open fun appendScriptEnv(env: MutableMap<String, String>) {}
}
