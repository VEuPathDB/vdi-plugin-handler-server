package vdi.service

import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.time.Duration
import kotlin.io.path.createDirectory
import kotlin.io.path.deleteIfExists
import kotlin.io.path.pathString
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import vdi.Const.ScriptEnvKey
import vdi.components.io.LineListOutputStream
import vdi.components.io.LoggingOutputStream
import vdi.components.ldap.LDAP
import vdi.components.ldap.OracleNetDesc
import vdi.components.script.ScriptExecutor
import vdi.conf.Configuration
import vdi.conf.DatabaseConfiguration
import vdi.util.unpackAsTarGZ

private const val INSTALL_DIR_NAME = "install"

class InstallDataService(
  private val workspace: Path,
  private val vdiID: String,
  private val payload: Path,
  private val ldap: LDAP,
  private val executor: ScriptExecutor,
  private val dbEnvConfig: DatabaseConfiguration,
) {
  private val log = LoggerFactory.getLogger(javaClass)

  suspend fun processInstall() : List<String> {
    val dbLdapConfig = ldap.requireSingularOracleNetDesc(dbEnvConfig.ldap.value)
    val installDir   = workspace.resolve(INSTALL_DIR_NAME)
    val warnings     = ArrayList<String>(4)

    installDir.createDirectory()
    payload.unpackAsTarGZ(installDir)
    payload.deleteIfExists()

    executor.executeScript(
      Configuration.ServiceConfiguration.installDataScriptPath,
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

        waitFor(Configuration.ServiceConfiguration.installDataScriptMaxSeconds)

        job1.join()
        job2.join()

        when (exitCode()) {
          0    -> {}
          else -> throw IllegalStateException("install script failed with unexpected exit code")
        }
      }
    }

    return warnings
  }
}