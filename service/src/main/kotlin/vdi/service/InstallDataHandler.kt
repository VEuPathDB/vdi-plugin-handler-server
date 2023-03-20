package vdi.service

import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectory
import kotlin.io.path.deleteIfExists
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import vdi.components.io.LineListOutputStream
import vdi.components.io.LoggingOutputStream
import vdi.components.metrics.ScriptMetrics
import vdi.components.script.ScriptExecutor
import vdi.conf.ScriptConfiguration
import vdi.consts.ExitCode
import vdi.model.DatabaseDetails
import vdi.util.unpackAsTarGZ

private const val INSTALL_DIR_NAME = "install"

class InstallDataHandler(
  private val workspace: Path,
  private val vdiID:     String,
  private val payload:   Path,
  private val dbDetails: DatabaseDetails,
  private val executor:  ScriptExecutor,
  private val script:    ScriptConfiguration,
  private val metrics:   ScriptMetrics,
) {
  private val log = LoggerFactory.getLogger(javaClass)

  init {
    log.trace(
      "init(workspace={}, vdiID={}, payload={}, dbDetails={}, executor={}, script={}",
      workspace,
      vdiID,
      payload,
      dbDetails,
      executor,
      script
    )
  }

  suspend fun run() : List<String> {
    log.trace("processInstall()")

    val installDir   = workspace.resolve(INSTALL_DIR_NAME)
    val warnings     = ArrayList<String>(4)

    log.debug("creating install data directory {}", installDir)
    installDir.createDirectory()

    log.debug("unpacking {} as a .tar.gz file", payload)
    payload.unpackAsTarGZ(installDir)
    payload.deleteIfExists()

    log.info("executing install-data script for VDI dataset ID {}", vdiID)
    val timer = metrics.installDataScriptDuration.startTimer()
    executor.executeScript(
      script.path,
      workspace,
      arrayOf(vdiID, installDir.absolutePathString()),
      dbDetails.toEnvMap()
    ) {
      coroutineScope {
        val job1 = launch { LoggingOutputStream("[install-data][$vdiID]", log).use { scriptStdErr.transferTo(it) } }
        val job2 = launch { LineListOutputStream(warnings).use { scriptStdOut.transferTo(it) } }

        waitFor(script.maxSeconds)

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

    return warnings
  }

  class ValidationError(val warnings: Collection<String>) : RuntimeException()
}