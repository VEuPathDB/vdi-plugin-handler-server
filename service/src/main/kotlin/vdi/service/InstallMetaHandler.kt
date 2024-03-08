package vdi.service

import org.slf4j.LoggerFactory
import org.veupathdb.vdi.lib.common.model.VDIDatasetMeta
import org.veupathdb.vdi.lib.json.JSON
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createFile
import kotlin.io.path.outputStream
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.veupathdb.vdi.lib.common.DatasetMetaFilename
import org.veupathdb.vdi.lib.common.field.DatasetID
import vdi.components.io.LoggingOutputStream
import vdi.components.metrics.ScriptMetrics
import vdi.components.script.ScriptExecutor
import vdi.conf.ScriptConfiguration
import vdi.consts.ExitStatus
import vdi.consts.FileName
import vdi.model.DatabaseDetails

class InstallMetaHandler(
  workspace: Path,
  vdiID: DatasetID,
  private val projectID: String,
  private val meta: VDIDatasetMeta,
  private val dbDetails: DatabaseDetails,
  executor:  ScriptExecutor,
  customPath: String,
  installPath: Path,
  private val script: ScriptConfiguration,
  metrics: ScriptMetrics,
) : InstallationHandlerBase<Unit>(vdiID, workspace, executor, customPath, installPath, metrics) {
  private val log = LoggerFactory.getLogger(javaClass)

  override suspend fun run() {

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
            log.info("install-meta script completed successfully for VDI dataset ID {}", datasetID)
          }

          else -> {
            log.error("install-meta script failed for VDI dataset ID {}", datasetID)
            throw IllegalStateException("install-meta script failed with unexpected exit code ${exitCode()}")
          }
        }
      }
    }

    timer.observeDuration()
  }

  override fun appendScriptEnv(env: MutableMap<String, String>) {
    super.appendScriptEnv(env)
    env.putAll(dbDetails.toEnvMap())
    env["PROJECT_ID"] = projectID
  }
}
