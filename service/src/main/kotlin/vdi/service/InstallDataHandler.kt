package vdi.service

import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.veupathdb.vdi.lib.common.model.VDIDatasetMeta
import org.veupathdb.vdi.lib.json.JSON
import java.io.OutputStreamWriter
import java.nio.file.Path
import kotlin.io.path.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.veupathdb.vdi.lib.common.DatasetManifestFilename
import org.veupathdb.vdi.lib.common.DatasetMetaFilename
import org.veupathdb.vdi.lib.common.field.DatasetID
import vdi.components.io.LineListOutputStream
import vdi.components.io.LoggingOutputStream
import vdi.components.metrics.ScriptMetrics
import vdi.components.script.ScriptExecutor
import vdi.conf.ScriptConfiguration
import vdi.consts.ExitStatus
import vdi.consts.FileName
import vdi.model.DatabaseDetails
import vdi.util.unpackAsZip
import java.io.IOException

class InstallDataHandler(
  workspace: Path,
  vdiID: DatasetID,
  private val projectID: String,
  private val payload: Path,
  private val dbDetails: DatabaseDetails,
  executor: ScriptExecutor,
  customPath: String,
  installPath: Path,
  private val metaScript: ScriptConfiguration,
  private val dataScript: ScriptConfiguration,
  private val compatScript: ScriptConfiguration,
  metrics: ScriptMetrics,
) : InstallationHandlerBase<List<String>>(vdiID, workspace, executor, customPath, installPath, metrics) {
  private val log = LoggerFactory.getLogger(javaClass)

  init {
    log.trace(
      "::init(workspace={}, vdiID={}, projectID={}, payload={}, dbDetails={}, executor={}, metaScript={}, dataScript={}, metrics={})",
      workspace,
      vdiID,
      projectID,
      payload,
      dbDetails,
      executor,
      metaScript,
      dataScript,
      metrics,
    )
  }

  override suspend fun run() : List<String> {
    log.trace("processInstall()")

    val installDir = workspace.resolve(FileName.InstallDirName)
    val warnings   = ArrayList<String>(4)

    log.debug("creating install data directory {}", installDir)
    installDir.createDirectory()

    log.debug("unpacking {} as a .zip file", payload)
    payload.unpackAsZip(installDir)
    payload.deleteIfExists()

    val metaFile = requireMetaFile(installDir)
    val metaData = JSON.readValue<VDIDatasetMeta>(metaFile.toFile())

    runInstallMeta(metaFile)

    if (metaData.dependencies.isNotEmpty())
      runCheckDependencies(metaFile)

    metaFile.deleteIfExists()
    getManifestFile(installDir).deleteIfExists()

    runInstallData(installDir, warnings)

    return warnings
  }

  override fun appendScriptEnv(env: MutableMap<String, String>) {
    super.appendScriptEnv(env)
    env.putAll(dbDetails.toEnvMap())
    env["PROJECT_ID"] = projectID
  }

  private suspend fun runInstallMeta(metaFile: Path) {
    val timer = metrics.installMetaScriptDuration.startTimer()
    log.info("executing install-meta (for install-data) script for VDI dataset ID {}", datasetID)
    executor.executeScript(metaScript.path, workspace, arrayOf(datasetID.toString(), metaFile.absolutePathString()), buildScriptEnv()) {
      coroutineScope {
        val logJob = launch { LoggingOutputStream("[install-meta][$datasetID]", log).use { scriptStdErr.transferTo(it) } }

        waitFor(metaScript.maxSeconds)

        logJob.join()

        metrics.installMetaCalls.labels(exitCode().toString()).inc()

        when (exitCode()) {
          0 -> {
            log.info("install-meta (for install-data) script completed successfully for VDI dataset ID {}", datasetID)
          }

          else -> {
            log.error("install-meta (for install-data) script failed for VDI dataset ID {}", datasetID)
            throw IllegalStateException("install-meta (for install-data) script failed with unexpected exit code ${exitCode()}")
          }
        }
      }
    }

    timer.observeDuration()
  }

  private suspend fun runCheckDependencies(metaFile: Path) {
    log.info("executing check-compatibility script for VDI dataset ID {}", datasetID)
    val timer    = metrics.checkCompatScriptDuration.startTimer()
    val warnings = ArrayList<String>()
    val meta     = JSON.readValue<VDIDatasetMeta>(metaFile.inputStream())

    executor.executeScript(
      compatScript.path,
      workspace,
      emptyArray(),
      buildScriptEnv()
    ) {
      coroutineScope {

        val logJob  = launch { LoggingOutputStream("[check-compatibility][$datasetID]", log).use { scriptStdErr.transferTo(it) } }
        val warnJob = launch { LineListOutputStream(warnings).use { scriptStdOut.transferTo(it) } }

        val osw = OutputStreamWriter(scriptStdIn)
        try {
          for (dep in meta.dependencies)
            osw.appendLine("${dep.identifier}\t${dep.version}")
          osw.flush()
        } catch (e: IOException) {
          log.error("Encountered error while attempting to write to process stdin:", e)
          if (isAlive()) {
            throw e
          }
        } finally {
          try {
            osw.close()
          } catch (e: IOException) {
            log.error("Encountered error while attempting to close process stdin:", e)
            if (isAlive()) {
              throw e
            }
          }
        }

        waitFor(compatScript.maxSeconds)

        logJob.join()
        warnJob.join()

        val compatStatus = ExitStatus.CheckCompatibility.fromCode(exitCode())

        metrics.checkCompatCalls.labels(compatStatus.metricFriendlyName).inc()

        when (compatStatus) {
          ExitStatus.CheckCompatibility.Success -> {
            log.info("check-compatibility script completed successfully for dataset ID {}", datasetID)
          }

          ExitStatus.CheckCompatibility.Incompatible -> {
            log.info("check-compatibility script completed with 'incompatible' for dataset ID {}", datasetID)
            throw CompatibilityError(warnings)
          }

          else -> {
            log.error("check-compatibility script failed for dataset ID {}", datasetID)
            throw IllegalStateException("check-compatibility script failed with exit code ${exitCode()}")
          }
        }
      }
    }
    timer.observeDuration()
  }

  private suspend fun runInstallData(installDir: Path, warnings: MutableList<String>) {
    log.info("executing install-data script for VDI dataset ID {}", datasetID)
    val timer = metrics.installDataScriptDuration.startTimer()
    executor.executeScript(
      dataScript.path,
      workspace,
      arrayOf(datasetID.toString(), installDir.absolutePathString()),
      buildScriptEnv()
    ) {
      coroutineScope {
        val job1 = launch { LoggingOutputStream("[install-data][$datasetID]", log).use { scriptStdErr.transferTo(it) } }
        val job2 = launch { LineListOutputStream(warnings).use { scriptStdOut.transferTo(it) } }

        waitFor(dataScript.maxSeconds)

        job1.join()
        job2.join()

        val installStatus = ExitStatus.InstallData.fromCode(exitCode())

        metrics.installDataCalls.labels(installStatus.metricFriendlyName).inc()

        when (installStatus) {
          ExitStatus.InstallData.Success -> {
            log.info("install-data script completed successfully for VDI dataset ID {}", datasetID)
          }

          ExitStatus.InstallData.ValidationFailure -> {
            log.info("install-data script refused to install VDI dataset {} for validation errors", datasetID)
            throw ValidationError(warnings)
          }

          else -> {
            log.error("install-data script failed for VDI dataset ID {}", datasetID)
            throw IllegalStateException("install-data script failed with unexpected exit code ${exitCode()}")
          }
        }
      }
    }
    timer.observeDuration()
  }

  private fun requireMetaFile(installDir: Path): Path {
    val metaFile = installDir.resolve(DatasetMetaFilename)

    if (!metaFile.exists()) {
      log.error("no meta file was found in the install directory for VDI dataset {}", datasetID)
      throw IllegalStateException("no meta file was found in the install directory for VDI dataset $datasetID")
    }

    return metaFile
  }

  private fun getManifestFile(installDir: Path): Path {
    return installDir.resolve(DatasetManifestFilename)
  }

  class ValidationError(val warnings: Collection<String>) : RuntimeException()

  class CompatibilityError(val warnings: Collection<String>) : RuntimeException()
}