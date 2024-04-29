package vdi.service

import org.slf4j.LoggerFactory
import org.veupathdb.vdi.lib.json.JSON
import java.nio.file.Path
import kotlin.io.path.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.veupathdb.vdi.lib.common.DatasetManifestFilename
import org.veupathdb.vdi.lib.common.DatasetMetaFilename
import org.veupathdb.vdi.lib.common.OriginTimestamp
import org.veupathdb.vdi.lib.common.compression.Zip
import org.veupathdb.vdi.lib.common.intra.ImportRequest
import org.veupathdb.vdi.lib.common.model.VDIDatasetFileInfoImpl
import org.veupathdb.vdi.lib.common.model.VDIDatasetManifestImpl
import vdi.components.io.LineListOutputStream
import vdi.components.io.LoggingOutputStream
import vdi.components.metrics.ScriptMetrics
import vdi.components.script.ScriptExecutor
import vdi.conf.ScriptConfiguration
import vdi.consts.ExitStatus
import vdi.consts.ScriptEnvKey
import vdi.util.Base36
import vdi.util.DoubleFmt
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

private const val INPUT_DIRECTORY_NAME  = "input"
private const val OUTPUT_DIRECTORY_NAME = "output"
private const val WARNING_FILE_NAME     = "warnings.json"
private const val OUTPUT_FILE_NAME      = "output.zip"

class ImportHandler(
  workspace: Path,
  private val inputFile: Path,
  private val details: ImportRequest,
  executor: ScriptExecutor,
  private val script: ScriptConfiguration,
  customPath: String,
  metrics: ScriptMetrics,
) : HandlerBase<Path>(details.vdiID, workspace, executor, customPath, metrics) {
  private val log = LoggerFactory.getLogger(javaClass)

  private val inputDirectory: Path = workspace.resolve(INPUT_DIRECTORY_NAME)
    .createDirectory()

  private val outputDirectory: Path = workspace.resolve(OUTPUT_DIRECTORY_NAME)
    .createDirectory()

  @OptIn(ExperimentalPathApi::class)
  override suspend fun run(): Path {
    val inputFiles = unpackInput()
    val warnings   = executeScript()

    val outputFiles = collectOutputFiles()
      .apply {
        add(writeManifestFile(inputFiles, this))
        add(writeMetaFile())
        add(writeWarningFile(warnings))
      }

    inputDirectory.deleteRecursively()

    return workspace.resolve(OUTPUT_FILE_NAME)
      .also { Zip.compress(it, outputFiles, Zip.Level(0u)) }
      .also { outputDirectory.deleteRecursively() }
  }

  override fun appendScriptEnv(env: MutableMap<String, String>) {
    super.appendScriptEnv(env)
    env[ScriptEnvKey.ImportID] = generateImportID()
  }

  @Suppress("NOTHING_TO_INLINE")
  private inline fun generateImportID() =
    Base36.encodeToString(OriginTimestamp.until(OffsetDateTime.now(), ChronoUnit.SECONDS).toULong()) +
      Base36.encodeToString(details.importIndex.toULong())


  /**
   * Unpacks the given input archive into the input directory and ensures that
   * the archive contained at least one input file.
   *
   * @return A collection of [Pair]s containing the paths to the files in the
   * input directory paired with the sizes of those files.  The sizes are used
   * to build the `vdi-manifest.json` file.
   */
  private fun unpackInput(): Collection<Pair<Path, Long>> {
    Zip.zipEntries(inputFile).forEach { (entry, inp) ->
      val file = inputDirectory.resolve(entry.name)
      file.outputStream().use { out -> inp.transferTo(out) }
    }

    inputFile.deleteExisting()

    val inputFiles = inputDirectory.listDirectoryEntries()

    if (inputFiles.isEmpty())
      throw EmptyInputError()

    return inputFiles.map { it to it.fileSize() }
  }

  /**
   * Executes the import script.
   *
   * If the import script fails execution this method will raise an appropriate
   * exception.
   *
   * If the import script returns a status code of
   * [ExitStatus.Import.Success], this method returns normally.
   *
   * If the import script returns a status code of
   * [ExitStatus.Import.ValidationFailure], this method will throw a
   * [ValidationError] exception.
   *
   * If the import script returns any other status code, this method will throw
   * an [IllegalStateException].
   *
   * @return A collection of warnings raised by the import script during its
   * execution.
   */
  private suspend fun executeScript(): Collection<String> {
    val timer = metrics.importScriptDuration.startTimer()

    log.info("executing import script for VDI dataset ID {}", details.vdiID)
    val warnings = executor.executeScript(
      script.path,
      workspace,
      arrayOf(inputDirectory.absolutePathString(), outputDirectory.absolutePathString()),
      buildScriptEnv(),
    ) {
      coroutineScope {
        val warnings = ArrayList<String>(8)

        val j1 = launch { LineListOutputStream(warnings).use { scriptStdOut.transferTo(it) } }
        val j2 = launch { LoggingOutputStream("[import][${details.vdiID}]", log).use { scriptStdErr.transferTo(it) } }

        waitFor(script.maxSeconds)

        j1.join()
        j2.join()

        val importStatus = ExitStatus.Import.fromCode(exitCode())

        metrics.importScriptCalls.labels(importStatus.metricFriendlyName).inc()

        when (importStatus) {
          ExitStatus.Import.Success -> {
            val dur = timer.observeDuration()
            log.info("import script completed successfully for dataset {} in {} seconds", details.vdiID, DoubleFmt.format(dur))
          }

          ExitStatus.Import.ValidationFailure -> {
            log.info("import script rejected dataset {} for validation error(s)", datasetID)
            throw ValidationError(warnings)
          }

          else -> {
            val err = "import script failed for dataset $datasetID with exit code ${exitCode()}"
            log.error(err)
            throw IllegalStateException(err)
          }
        }

        warnings
      }
    }

    return warnings
  }

  private fun collectOutputFiles() : MutableCollection<Path> {
    // Collect a list of the files that the import script spit out.
    val outputFiles = outputDirectory.listDirectoryEntries()
      .toMutableList()

    // Ensure that _something_ was produced.
    if (outputFiles.isEmpty())
      throw IllegalStateException("import script produced no output files")

    return outputFiles
  }

  private fun writeManifestFile(inputFiles: Collection<Pair<Path, Long>>, outputFiles: Collection<Path>) =
    outputDirectory.resolve(DatasetManifestFilename)
      .createFile()
      .apply { outputStream().use { JSON.writeValue(it, buildManifest(inputFiles, outputFiles)) } }

  private fun writeMetaFile() =
    outputDirectory.resolve(DatasetMetaFilename)
      .createFile()
      .apply { outputStream().use { JSON.writeValue(it, details.meta) } }

  private fun writeWarningFile(warnings: Collection<String>) =
    outputDirectory.resolve(WARNING_FILE_NAME)
      .createFile()
      .apply { outputStream().use { JSON.writeValue(it, WarningsFile(warnings)) } }

  private fun buildManifest(inputFiles: Collection<Pair<Path, Long>>, outputFiles: Collection<Path>) =
    VDIDatasetManifestImpl(
      inputFiles = inputFiles.map { VDIDatasetFileInfoImpl(it.first.name, it.second) },
      dataFiles = outputFiles.map { VDIDatasetFileInfoImpl(it.name, it.fileSize()) },
    )

  class EmptyInputError : RuntimeException("input archive contained no files")

  class ValidationError(val warnings: Collection<String>) : RuntimeException()

  data class WarningsFile(val warnings: Collection<String>)
}