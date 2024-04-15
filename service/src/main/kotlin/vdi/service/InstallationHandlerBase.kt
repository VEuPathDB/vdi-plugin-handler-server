package vdi.service

import org.slf4j.LoggerFactory
import org.veupathdb.vdi.lib.common.field.DatasetID
import org.veupathdb.vdi.lib.common.field.ProjectID
import vdi.components.metrics.ScriptMetrics
import vdi.components.script.ScriptExecutor
import vdi.consts.ScriptEnvKey
import vdi.model.DatabaseDetails
import java.nio.file.Path
import kotlin.io.path.exists

sealed class InstallationHandlerBase<T>(
  datasetID: DatasetID,
  jobID: ULong,

  protected val projectID: ProjectID,

  workspace: Path,
  executor: ScriptExecutor,
  customPath: String,

  /**
   * User dataset install path
   */
  protected val datasetInstallPath: Path,

  metrics: ScriptMetrics,

  private val dbDetails: DatabaseDetails,
) : HandlerBase<T>(datasetID, jobID, workspace, executor, customPath, metrics) {
  private val logger = LoggerFactory.getLogger(javaClass)

  final override suspend fun run(): T {
    if (datasetInstallPath.exists()) {
      val errMsg = "install directory already exists for dataset $datasetID before install script has been run"
      logger.error(errMsg)
      throw IllegalStateException(errMsg)
    }

    return runJob()
  }

  abstract suspend fun runJob(): T

  override fun appendScriptEnv(env: MutableMap<String, String>) {
    env[ScriptEnvKey.InstallPath] = datasetInstallPath.toString()
    env[ScriptEnvKey.ProjectID] = projectID

    env.putAll(dbDetails.toEnvMap())
  }
}