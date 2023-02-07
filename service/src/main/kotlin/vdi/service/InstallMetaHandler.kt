package vdi.service

import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.outputStream
import kotlin.io.path.pathString
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import vdi.components.common.SecretString
import vdi.components.io.LoggingOutputStream
import vdi.components.json.JSON
import vdi.components.script.ScriptExecutor
import vdi.conf.ScriptConfiguration
import vdi.consts.ScriptEnvKey
import vdi.server.model.DatasetMeta

private const val META_FILE_NAME = "meta.json"

class InstallMetaHandler(
  private val workspace: Path,
  private val vdiID: String,
  private val meta: DatasetMeta,
  private val dbDetails: DatabaseDetails,
  private val executor: ScriptExecutor,
  private val script: ScriptConfiguration,
) {
  private val log = LoggerFactory.getLogger(javaClass)

  suspend fun run() {
    log.trace("run()")

    log.debug("writing meta file out to workspace {}", workspace)
    val metaFile = workspace.resolve(META_FILE_NAME)
      .createFile()
      .apply { outputStream().use { JSON.writeValue(it, meta) } }

    log.debug("calling install-meta script for VDI dataset ID {}", vdiID)
    executor.executeScript(script.path, workspace, arrayOf(vdiID, metaFile.pathString), dbDetails.toEnvMap()) {
      coroutineScope {
        val logJob = launch { LoggingOutputStream(log).use { scriptStdErr.transferTo(it) } }

        waitFor(script.maxSeconds)

        logJob.join()

        when (exitCode()) {
          0 -> {
            log.debug("install-meta script completed successfully for VDI dataset ID {}", vdiID)
          }

          else -> {
            log.error("install-meta script failed for VDI dataset ID {}", vdiID)
            throw IllegalStateException("install-meta script failed with unexpected exit code")
          }
        }
      }
    }
  }

  data class DatabaseDetails(
    val dbHost: String,
    val dbPort: UShort,
    val dbName: String,
    val dbUser: SecretString,
    val dbPass: SecretString,
  ) {
    fun toEnvMap() = mapOf(
      ScriptEnvKey.DBHost to dbHost,
      ScriptEnvKey.DBPort to dbPort.toString(),
      ScriptEnvKey.DBName to dbName,
      ScriptEnvKey.DBUser to dbUser.value,
      ScriptEnvKey.DBPass to dbPass.value,
    )
  }
}