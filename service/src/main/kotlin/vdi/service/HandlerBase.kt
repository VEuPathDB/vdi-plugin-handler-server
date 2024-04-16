package vdi.service

import org.veupathdb.vdi.lib.common.OriginTimestamp
import org.veupathdb.vdi.lib.common.env.Environment
import org.veupathdb.vdi.lib.common.field.DatasetID
import java.nio.file.Path
import vdi.components.metrics.ScriptMetrics
import vdi.components.script.ScriptExecutor
import vdi.consts.ScriptEnvKey
import vdi.util.Base36
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

sealed class HandlerBase<T>(
  /**
   * VDI ID of the dataset to which the data being processed belongs.
   */
  protected val datasetID: DatasetID,

  /**
   * Job ID of the current task, assigned by the calling core VDI service.
   */
  protected val jobID: ULong,

  /**
   * Path to the workspace for the script execution.
   */
  protected val workspace: Path,

  /**
   * Script executor service.
   */
  protected val executor: ScriptExecutor,

  /**
   * Custom PATH environment variable entries
   */
  protected val customPath: String,

  /**
   * Metrics collector.
   */
  protected val metrics: ScriptMetrics,
) {
  abstract suspend fun run(): T

  protected fun buildScriptEnv(): Environment {
    val out = System.getenv().toMutableMap() // Copy the environment before appending script env.

    if (customPath.isBlank())
      out["PATH"] = System.getenv("PATH")
    else
      out["PATH"] = System.getenv("PATH") + ':' + customPath

    out[ScriptEnvKey.DatasetID] = datasetID.toString()
    out[ScriptEnvKey.JobID] = generateJobID()

    appendScriptEnv(out)

    return out
  }

  protected open fun appendScriptEnv(env: MutableMap<String, String>) {}

  private fun generateJobID() =
    Base36.encodeToString(OriginTimestamp.until(OffsetDateTime.now(), ChronoUnit.SECONDS).toULong() + jobID)
}
