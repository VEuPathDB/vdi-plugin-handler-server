package vdi.service

import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import vdi.components.io.LoggingOutputStream
import vdi.components.metrics.ScriptMetrics
import vdi.components.script.ScriptExecutor
import vdi.conf.ScriptConfiguration
import vdi.model.DatabaseDetails

class UninstallHandler(
  workspace: Path,
  private val vdiID: String,
  private val dbDetails: DatabaseDetails,
  executor:  ScriptExecutor,
  customPath: String,
  private val script: ScriptConfiguration,
  metrics: ScriptMetrics,
) : HandlerBase<Unit>(workspace, executor, customPath, metrics) {
  private val log = LoggerFactory.getLogger(javaClass)

  override suspend fun run() {
    log.trace("run()")

    log.info("executing uninstall script for VDI dataset ID {}", vdiID)
    val timer = metrics.uninstallScriptDuration.startTimer()
    executor.executeScript(script.path, workspace, arrayOf(vdiID), buildScriptEnv()) {
      coroutineScope {
        val logJob = launch { LoggingOutputStream("[uninstall][$vdiID]", log).use { scriptStdErr.transferTo(it) } }

        waitFor(script.maxSeconds)

        logJob.join()

        metrics.uninstallCalls.labels(exitCode().toString()).inc()

        when (exitCode()) {
          0 -> {
            log.debug("uninstall script completed successfully for VDI dataset ID {}", vdiID)
          }

          else -> {
            log.error("uninstall script failed for VDI dataset ID {}", vdiID)
            throw IllegalStateException("uninstall script failed with an unexpected exit code")
          }
        }
      }
    }

    timer.observeDuration()
  }

  override fun appendScriptEnv(env: MutableMap<String, String>) {
    env.putAll(dbDetails.toEnvMap())
  }
}