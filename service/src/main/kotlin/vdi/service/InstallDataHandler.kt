package vdi.service

import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.deleteIfExists
import kotlin.io.path.pathString
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import vdi.components.io.LineListOutputStream
import vdi.components.io.LoggingOutputStream
import vdi.components.ldap.LDAP
import vdi.components.script.ScriptExecutor
import vdi.conf.DatabaseConfiguration
import vdi.conf.ScriptConfiguration
import vdi.consts.ScriptEnvKey
import vdi.util.unpackAsTarGZ

private const val INSTALL_DIR_NAME = "install"

class InstallDataHandler(
  private val workspace: Path,
  private val vdiID: String,
  private val payload: Path,
  private val ldap: LDAP,
  private val executor: ScriptExecutor,
  private val dbEnvConfig: DatabaseConfiguration,
  private val script: ScriptConfiguration,
) {
  private val log = LoggerFactory.getLogger(javaClass)

  init {
    log.trace("init(workspace={}, vdiID={}, payload={}, ldap={}, executor={}, dbEnvConfig={}", workspace, vdiID, payload, ldap, executor, dbEnvConfig)
  }

  suspend fun run() : List<String> {
    log.trace("processInstall()")

    val dbLdapConfig = ldap.requireSingularOracleNetDesc(dbEnvConfig.ldap.value)
    val installDir   = workspace.resolve(INSTALL_DIR_NAME)
    val warnings     = ArrayList<String>(4)

    log.debug("creating install data directory {}", installDir)
    installDir.createDirectory()

    log.debug("unpacking {} as a .tar.gz file", payload)
    payload.unpackAsTarGZ(installDir)
    payload.deleteIfExists()

    log.info("executing install-data script for VDI dataset ID {}", vdiID)
    executor.executeScript(
      script.path,
      workspace,
      arrayOf(vdiID, installDir.pathString),
      mapOf(
        ScriptEnvKey.DBHost to dbLdapConfig.host,
        ScriptEnvKey.DBPort to dbLdapConfig.port.toString(),
        ScriptEnvKey.DBName to dbLdapConfig.serviceName,
        ScriptEnvKey.DBUser to dbEnvConfig.user.value,
        ScriptEnvKey.DBPass to dbEnvConfig.pass.value,
      )
    ) {
      coroutineScope {
        val job1 = launch { LoggingOutputStream(log).use { scriptStdErr.transferTo(it) } }
        val job2 = launch { LineListOutputStream(warnings).use { scriptStdOut.transferTo(it) } }

        waitFor(script.maxSeconds)

        job1.join()
        job2.join()

        when (exitCode()) {
          0    -> {
            log.debug("install-data script completed successfully for VDI dataset ID {}", vdiID)
          }

          else -> {
            log.error("install-data script failed for VDI dataset ID {}", vdiID)
            throw IllegalStateException("install script failed with unexpected exit code")
          }
        }
      }
    }

    return warnings
  }
}