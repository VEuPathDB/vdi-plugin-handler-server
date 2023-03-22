package vdi.service

import org.slf4j.LoggerFactory
import java.nio.file.Path
import vdi.components.metrics.ScriptMetrics
import vdi.components.script.ScriptExecutor

sealed class HandlerBase<T>(
  protected val workspace: Path,
  protected val executor: ScriptExecutor,
  protected val metrics: ScriptMetrics,
) {
  private val log = LoggerFactory.getLogger(javaClass)

  init {
    log.trace("::init(workspace={}, executor={}, metrics={})", workspace, executor, metrics)
  }

  abstract suspend fun run(): T

  protected abstract fun buildScriptEnv(): Map<String, String>
}
