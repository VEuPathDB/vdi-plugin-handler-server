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
import org.veupathdb.vdi.lib.common.DatasetMetaFilename
import vdi.components.io.LineListOutputStream
import vdi.components.io.LoggingOutputStream
import vdi.components.metrics.ScriptMetrics
import vdi.components.script.ScriptExecutor
import vdi.conf.ScriptConfiguration
import vdi.consts.ExitStatus
import vdi.consts.FileName
import vdi.model.DatabaseDetails
import vdi.server.context.InstallDataContext
import vdi.util.DoubleFmt
import vdi.util.unpackAsZip
import java.io.IOException
import vdi.components.script.PluginScript
import vdi.components.script.PluginScriptException

class InstallDataHandler(
  private val installCtx: InstallDataContext,
  dbDetails: DatabaseDetails,
  executor: ScriptExecutor,
  customPath: String,
  datasetInstallPath: Path,
  private val metaScript: ScriptConfiguration,
  private val dataScript: ScriptConfiguration,
  private val compatScript: ScriptConfiguration,
  metrics: ScriptMetrics,
) : InstallationHandlerBase<List<String>>(
  installCtx.request.vdiID,
  installCtx.request.projectID,
  installCtx.workspace,
  executor,
  customPath,
  datasetInstallPath,
  metrics,
  dbDetails
) {
  private val log = LoggerFactory.getLogger(javaClass)

  init {
    if (datasetInstallPath.exists()) {
      val msg = "dataset install directory already exists: ${datasetInstallPath.relativeTo(datasetInstallPath.parent.parent.parent)}"
      log.error(msg)
      throw InstallDirConflictError(msg)
    }
  }

  override suspend fun runJob() : List<String> {
    val installWorkspace = workspace.resolve(FileName.InstallDirName)
    val warnings = ArrayList<String>(4)

    log.debug("creating install data workspace {}", installWorkspace)
    installWorkspace.createDirectory()

    log.debug("unpacking {} as a .zip file", installCtx.payload)
    installCtx.payload.unpackAsZip(installWorkspace)
    installCtx.payload.deleteIfExists()

    val metaFile = writeMetaFile(installWorkspace, installCtx.meta)
    runInstallMeta(metaFile)

    if (installCtx.meta.dependencies.isNotEmpty())
      runCheckDependencies(metaFile)

    metaFile.deleteIfExists()

    runInstallData(installWorkspace, warnings)

    return warnings
  }

  private suspend fun runInstallMeta(metaFile: Path) {
    val timer = metrics.installMetaScriptDuration.startTimer()
    log.info("executing install-meta (for install-data) script for VDI dataset ID {}", datasetID)
    executor.executeScript(metaScript.path, workspace, arrayOf(datasetID.toString(), metaFile.absolutePathString()), buildScriptEnv()) {
      coroutineScope {
        val logJob = launch { LoggingOutputStream("[install-meta][$datasetID]", log).use { scriptStdErr.transferTo(it) } }

        waitFor(metaScript.maxDuration)

        logJob.join()

        metrics.installMetaCalls.labelValues(ExitStatus.InstallMeta.fromCode(exitCode()).metricFriendlyName).inc()

        when (exitCode()) {
          0 -> {
            val dur = timer.observeDuration()
            log.info("install-meta (for install-data) script completed successfully for dataset {} in {} seconds", datasetID, DoubleFmt.format(dur))
          }

          else -> {
            val err = "install-meta (for install-data) script failed for dataset $datasetID with exit code ${exitCode()}"
            log.error(err)
            throw PluginScriptException(PluginScript.InstallMeta, err)
          }
        }
      }
    }
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

        waitFor(compatScript.maxDuration)

        logJob.join()
        warnJob.join()

        val compatStatus = ExitStatus.CheckCompatibility.fromCode(exitCode())

        metrics.checkCompatCalls.labelValues(compatStatus.metricFriendlyName).inc()

        when (compatStatus) {
          ExitStatus.CheckCompatibility.Success -> {
            val dur = timer.observeDuration()
            log.info("check-compatibility script completed successfully for dataset ID {} in {} seconds", datasetID, DoubleFmt.format(dur))
          }

          ExitStatus.CheckCompatibility.Incompatible -> {
            log.info("check-compatibility script completed with 'incompatible' for dataset ID {}", datasetID)
            throw CompatibilityError(warnings)
          }

          else -> {
            val err = "check-compatibility script failed for dataset $datasetID with exit code ${exitCode()}"
            log.error(err)
            throw PluginScriptException(PluginScript.CheckCompatibility, err)
          }
        }
      }
    }
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

        waitFor(dataScript.maxDuration)

        job1.join()
        job2.join()

        val installStatus = ExitStatus.InstallData.fromCode(exitCode())

        metrics.installDataCalls.labelValues(installStatus.metricFriendlyName).inc()

        when (installStatus) {
          ExitStatus.InstallData.Success -> {
            val dur = timer.observeDuration()
            log.info("install-data script completed successfully for VDI dataset ID {} in {} seconds", datasetID, DoubleFmt.format(dur))
          }

          ExitStatus.InstallData.ValidationFailure -> {
            log.info("install-data script refused to install VDI dataset {} for validation errors", datasetID)
            throw ValidationError(warnings)
          }

          else -> {
            val err = "install-data script failed for dataset $datasetID with exit code ${exitCode()}"
            log.error(err)
            throw PluginScriptException(PluginScript.InstallData, err)
          }
        }
      }
    }
  }

  class ValidationError(val warnings: Collection<String>) : RuntimeException()

  class CompatibilityError(val warnings: Collection<String>) : RuntimeException()

  class InstallDirConflictError(msg: String) : RuntimeException(msg)
}

private fun writeMetaFile(installDir: Path, meta: VDIDatasetMeta): Path {
  val metaFile = installDir.resolve(DatasetMetaFilename)
  metaFile.createFile()
  metaFile.outputStream().buffered().use { JSON.writeValue(it, meta) }
  return metaFile
}
