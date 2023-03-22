package vdi.service

import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import vdi.components.io.LineListOutputStream
import vdi.components.io.LoggingOutputStream
import vdi.components.metrics.ScriptMetrics
import vdi.components.script.ScriptExecutor
import vdi.conf.ScriptConfiguration
import vdi.consts.ExitCode
import vdi.consts.FileName
import vdi.model.DatabaseDetails
import vdi.util.unpackAsTarGZ

class InstallDataHandler(
  workspace: Path,
  private val vdiID: String,
  private val projectID: String,
  private val payload: Path,
  private val dbDetails: DatabaseDetails,
  executor: ScriptExecutor,
  private val metaScript: ScriptConfiguration,
  private val dataScript: ScriptConfiguration,
  metrics: ScriptMetrics,
) : HandlerBase<List<String>>(workspace, executor, metrics) {
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

    val installDir   = workspace.resolve(FileName.InstallDirName)
    val warnings     = ArrayList<String>(4)

    log.debug("creating install data directory {}", installDir)
    installDir.createDirectory()

    log.debug("unpacking {} as a .tar.gz file", payload)
    payload.unpackAsTarGZ(installDir)
    payload.deleteIfExists()

    runInstallMeta(installDir)
    runInstallData(installDir, warnings)

    return warnings
  }

  private suspend fun runInstallMeta(installDir: Path) {
    val metaFile = installDir.resolve(FileName.MetaFileName)

    if (!metaFile.exists()) {
      log.error("no meta file was found in the install directory for VDI dataset {}", vdiID)
      throw IllegalStateException("no meta file was found in the install directory for VDI dataset $vdiID")
    }

    val timer = metrics.installMetaScriptDuration.startTimer()
    log.info("executing install-meta (for install-data) script for VDI dataset ID {}", vdiID)
    executor.executeScript(metaScript.path, workspace, arrayOf(vdiID, metaFile.absolutePathString()), buildScriptEnv()) {
      coroutineScope {
        val logJob = launch { LoggingOutputStream("[install-meta][$vdiID]", log).use { scriptStdErr.transferTo(it) } }

        waitFor(metaScript.maxSeconds)

        logJob.join()

        when (exitCode()) {
          0 -> {
            log.info("install-meta (for install-data) script completed successfully for VDI dataset ID {}", vdiID)
          }

          else -> {
            log.error("install-meta (for install-data) script failed for VDI dataset ID {}", vdiID)
            throw IllegalStateException("install-meta (for install-data) script failed with unexpected exit code")
          }
        }
      }
    }

    timer.observeDuration()

  }

  private suspend fun runInstallData(installDir: Path, warnings: MutableList<String>) {
    log.info("executing install-data script for VDI dataset ID {}", vdiID)
    val timer = metrics.installDataScriptDuration.startTimer()
    executor.executeScript(
      dataScript.path,
      workspace,
      arrayOf(vdiID, installDir.absolutePathString()),
      buildScriptEnv()
    ) {
      coroutineScope {
        val job1 = launch { LoggingOutputStream("[install-data][$vdiID]", log).use { scriptStdErr.transferTo(it) } }
        val job2 = launch { LineListOutputStream(warnings).use { scriptStdOut.transferTo(it) } }

        waitFor(dataScript.maxSeconds)

        job1.join()
        job2.join()

        when (exitCode()) {
          ExitCode.InstallScriptSuccess -> {
            log.info("install-data script completed successfully for VDI dataset ID {}", vdiID)
          }

          ExitCode.InstallScriptValidationFailure -> {
            log.info("install-data script refused to install VDI dataset {} for validation errors", vdiID)
            throw ValidationError(warnings)
          }

          else -> {
            log.error("install-data script failed for VDI dataset ID {}", vdiID)
            throw IllegalStateException("install-data script failed with unexpected exit code")
          }
        }
      }
    }
    timer.observeDuration()
  }

  override fun buildScriptEnv(): Map<String, String> {
    val out = HashMap<String, String>(12)
    out.putAll(dbDetails.toEnvMap())
    out["PROJECT_ID"] = projectID
    return out
  }

  class ValidationError(val warnings: Collection<String>) : RuntimeException()
}