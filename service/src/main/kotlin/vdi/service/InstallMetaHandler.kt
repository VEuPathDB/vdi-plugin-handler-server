package vdi.service

import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createFile
import kotlin.io.path.outputStream
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import vdi.components.io.LoggingOutputStream
import vdi.components.json.JSON
import vdi.components.metrics.ScriptMetrics
import vdi.components.script.ScriptExecutor
import vdi.conf.ScriptConfiguration
import vdi.consts.FileName
import vdi.model.DatabaseDetails
import vdi.server.model.DatasetMeta

class InstallMetaHandler(
  private val workspace: Path,
  private val vdiID:     String,
  private val meta:      DatasetMeta,
  private val dbDetails: DatabaseDetails,
  private val executor:  ScriptExecutor,
  private val script:    ScriptConfiguration,
  private val metrics:   ScriptMetrics,
) {
  private val log = LoggerFactory.getLogger(javaClass)

  suspend fun run() {

    val metaFile = workspace.resolve(FileName.MetaFileName)
      .createFile()
      .apply { outputStream().use { JSON.writeValue(it, meta) } }

    val timer = metrics.installMetaScriptDuration.startTimer()
    log.info("executing install-meta script for VDI dataset ID {}", vdiID)
    executor.executeScript(script.path, workspace, arrayOf(vdiID, metaFile.absolutePathString()), dbDetails.toEnvMap()) {
      coroutineScope {
        val logJob = launch { LoggingOutputStream("[install-meta][$vdiID]", log).use { scriptStdErr.transferTo(it) } }

        waitFor(script.maxSeconds)

        logJob.join()

        when (exitCode()) {
          0 -> {
            log.info("install-meta script completed successfully for VDI dataset ID {}", vdiID)
          }

          else -> {
            log.error("install-meta script failed for VDI dataset ID {}", vdiID)
            throw IllegalStateException("install-meta script failed with unexpected exit code")
          }
        }
      }
    }

    timer.observeDuration()
  }
}
