package vdi.service

import org.slf4j.LoggerFactory
import org.veupathdb.vdi.lib.json.JSON
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createFile
import kotlin.io.path.outputStream
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.veupathdb.vdi.lib.common.DatasetMetaFilename
import org.veupathdb.vdi.lib.common.intra.InstallMetaRequest
import vdi.components.io.LoggingOutputStream
import vdi.components.metrics.ScriptMetrics
import vdi.components.script.ScriptExecutor
import vdi.conf.ScriptConfiguration
import vdi.consts.ExitStatus
import vdi.model.DatabaseDetails
import vdi.util.DoubleFmt

class InstallMetaHandler(
  workspace: Path,
  request: InstallMetaRequest,
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
  dbDetails,
) {
  private val log = LoggerFactory.getLogger(javaClass)

  private val meta = request.meta

  override suspend fun runJob() {

    val metaFile = workspace.resolve(DatasetMetaFilename)
      .createFile()
      .apply { outputStream().use { JSON.writeValue(it, meta) } }

    val timer = metrics.installMetaScriptDuration.startTimer()
    log.info("executing install-meta script for VDI dataset ID {}", datasetID)
    executor.executeScript(script.path, workspace, arrayOf(datasetID.toString(), metaFile.absolutePathString()), buildScriptEnv()) {
      coroutineScope {
        val logJob = launch { LoggingOutputStream("[install-meta][$datasetID]", log).use { scriptStdErr.transferTo(it) } }

        waitFor(script.maxSeconds)

        logJob.join()

        val installMetaStatus = ExitStatus.InstallMeta.fromCode(exitCode())

        metrics.installMetaCalls.labels(installMetaStatus.metricFriendlyName).inc()

        when (installMetaStatus) {
          ExitStatus.InstallMeta.Success -> {
            val dur = timer.observeDuration()
            log.info("install-meta script completed successfully for dataset {} in {} seconds", datasetID, DoubleFmt.format(dur))
          }

          else -> {
            val err = "install-meta script failed for dataset $datasetID with exit code ${exitCode()}"
            log.error(err)
            throw IllegalStateException(err)
          }
        }
      }
    }
  }
}
