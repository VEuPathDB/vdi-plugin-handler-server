package vdi.service

import org.slf4j.LoggerFactory
import org.veupathdb.vdi.lib.common.env.Environment
import java.nio.file.Path
import vdi.components.metrics.ScriptMetrics
import vdi.components.script.ScriptExecutor

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

  protected fun buildScriptEnv(): Environment =
    if (customPath.isBlank())
      mutableMapOf("PATH" to System.getenv("PATH"))
    else
      mutableMapOf("PATH" to System.getenv("PATH") + ':' + customPath)

  protected open fun appendScriptEnv(env: MutableMap<String, String>) {}
}
