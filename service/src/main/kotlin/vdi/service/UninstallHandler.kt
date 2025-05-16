package vdi.service

import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.veupathdb.vdi.lib.common.intra.UninstallRequest
import vdi.components.io.LoggingOutputStream
import vdi.components.metrics.ScriptMetrics
import vdi.components.script.ScriptExecutor
import vdi.conf.ScriptConfiguration
import vdi.consts.ExitStatus
import vdi.model.DatabaseDetails
import vdi.util.DoubleFmt
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.moveTo
import vdi.components.script.PluginScript
import vdi.components.script.PluginScriptException

private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

class UninstallHandler(
  workspace: Path,
  request: UninstallRequest,
  dbDetails: DatabaseDetails,
  executor:  ScriptExecutor,
  customPath: String,
  installPath: Path,
  private val script: ScriptConfiguration,
  metrics: ScriptMetrics,
) : InstallationHandlerBase<Unit>(
  request.vdiID,
  request.projectID,
  workspace,
  executor,
  customPath,
  installPath,
  metrics,
  dbDetails
) {
  private val log = LoggerFactory.getLogger(javaClass)

  override suspend fun runJob() {
    log.info("executing uninstall script for VDI dataset ID {}", datasetID)

    val timer = metrics.uninstallScriptDuration.startTimer()

    executor.executeScript(script.path, workspace, arrayOf(datasetID.toString()), buildScriptEnv()) {
      coroutineScope {
        val logJob = launch { LoggingOutputStream("[uninstall][$datasetID]", log).use { scriptStdErr.transferTo(it) } }

        waitFor(script.maxDuration)

        logJob.join()

        val installStatus = ExitStatus.UninstallData.fromCode(exitCode())

        metrics.uninstallCalls.labelValues(installStatus.metricFriendlyName).inc()

        when (installStatus) {
          ExitStatus.UninstallData.Success -> {
            val dur = timer.observeDuration()
            log.info("uninstall script completed successfully for dataset {} in {} seconds", datasetID, DoubleFmt.format(dur))
            wipeDatasetDir()
          }

          else -> {
            val err = "uninstall script failed for dataset $datasetID with exit code ${exitCode()}"
            log.error(err)
            throw PluginScriptException(PluginScript.Uninstall, err)
          }
        }
      }
    }
  }

  @OptIn(ExperimentalPathApi::class)
  private fun wipeDatasetDir() {
    if (!datasetInstallPath.exists())
      return

    log.debug("attempting to delete dataset directory {}", datasetInstallPath)

    datasetInstallPath
      .moveTo(datasetInstallPath.parent.resolve("deleting-$datasetID-${LocalDateTime.now().format(dateFormat)}"))
      .deleteRecursively()

    log.info("deleted dataset directory {}", datasetInstallPath)
  }
}
