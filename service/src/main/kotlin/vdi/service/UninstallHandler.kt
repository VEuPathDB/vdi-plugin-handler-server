package vdi.service

import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import vdi.components.io.LoggingOutputStream
import vdi.components.script.ScriptExecutor
import vdi.conf.ScriptConfiguration
import vdi.model.DatabaseDetails

class UninstallHandler(
  private val workspace: Path,
  private val vdiID: String,
  private val dbDetails: DatabaseDetails,
  private val executor: ScriptExecutor,
  private val script: ScriptConfiguration,
) {
  private val log = LoggerFactory.getLogger(javaClass)

  suspend fun run() {
    log.trace("run()")

    log.debug("calling uninstall script for VDI dataset ID {}", vdiID)
    executor.executeScript(script.path, workspace, arrayOf(vdiID), dbDetails.toEnvMap()) {
      coroutineScope {
        val logJob = launch { LoggingOutputStream(log).use { scriptStdErr.transferTo(it) } }

        waitFor(script.maxSeconds)

        logJob.join()

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
  }
}