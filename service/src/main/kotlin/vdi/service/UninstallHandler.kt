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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.moveTo

private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

class UninstallHandler(
  workspace: Path,
  private val vdiID: String,
  private val dbDetails: DatabaseDetails,
  executor:  ScriptExecutor,
  customPath: String,
  installPath: Path,
  private val script: ScriptConfiguration,
  metrics: ScriptMetrics,
) : InstallationHandlerBase<Unit>(workspace, executor, customPath, installPath, metrics) {
  private val log = LoggerFactory.getLogger(javaClass)

  override suspend fun run() {
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
            log.info("uninstall script completed successfully for VDI dataset ID {}", vdiID)
            wipeDatasetDir()
          }

          else -> {
            log.error("uninstall script failed for VDI dataset ID {}", vdiID)
            throw IllegalStateException("uninstall script failed with an unexpected exit code ${exitCode()}")
          }
        }
      }
    }

    timer.observeDuration()
  }

  override fun appendScriptEnv(env: MutableMap<String, String>) {
    super.appendScriptEnv(env)
    env.putAll(dbDetails.toEnvMap())
  }

  @OptIn(ExperimentalPathApi::class)
  private fun wipeDatasetDir() {
    if (!datasetInstallPath.exists())
      return

    log.debug("attempting to delete dataset directory {}", datasetInstallPath)

    // Rename dataset directory to avoid any conflicts in the event of a
    // reinstall or failed directory deletion.
    datasetInstallPath
      .moveTo(datasetInstallPath.parent.resolve("deleting-$vdiID-${LocalDateTime.now().format(dateFormat)}"))
      .deleteRecursively()

    log.info("deleted dataset directory {}", datasetInstallPath)
  }
}