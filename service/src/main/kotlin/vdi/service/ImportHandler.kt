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
import org.veupathdb.vdi.lib.common.model.VDIDatasetFileInfo
import org.veupathdb.vdi.lib.common.model.VDIDatasetManifest
import vdi.components.io.LineListOutputStream
import vdi.components.io.LoggingOutputStream
import vdi.components.metrics.ScriptMetrics
import vdi.components.script.ScriptExecutor
import vdi.conf.ScriptConfiguration
import vdi.consts.ExitStatus
import vdi.consts.ScriptEnvKey
import vdi.server.context.ImportContext
import vdi.util.Base36
import vdi.util.DoubleFmt
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

private const val INPUT_DIRECTORY_NAME  = "input"
private const val OUTPUT_DIRECTORY_NAME = "output"
private const val WARNING_FILE_NAME     = "warnings.json"
private const val OUTPUT_FILE_NAME      = "output.zip"
private const val DATA_ZIP_NAME         = "data.zip"

/**
 * Executes 'import' dataset preprocessing and validation.
 *
 * @see [run] for more details.
 */
class ImportHandler(
  private val importCtx: ImportContext,
  executor: ScriptExecutor,
  private val script: ScriptConfiguration,
  customPath: String,
  metrics: ScriptMetrics,
) : HandlerBase<Path>(importCtx.request.vdiID, importCtx.workspace, executor, customPath, metrics) {
  private val log = LoggerFactory.getLogger(javaClass)

  private val inputDirectory: Path = workspace.resolve(INPUT_DIRECTORY_NAME)
    .createDirectory()

  private val outputDirectory: Path = workspace.resolve(OUTPUT_DIRECTORY_NAME)
    .createDirectory()

  /**
   * Performs the import preprocessing steps for a target dataset.
   *
   * The steps involved are:
   * 1. Unpack the posted input zip.
   * 2. Execute the plugin 'import' script on the input files.
   * 3. Zip the outputs from the plugin 'import' script.
   * 4. Write a manifest of the import inputs and outputs.
   * 5. Write a list of import warnings.
   * 6. Return an uncompressed zip stream containing the manifest, warnings, and
   *    compressed results.
   */
  @OptIn(ExperimentalPathApi::class)
  override suspend fun run(): Path {
    val inputFiles   = unpackInput()
    val warnings     = executeScript()
    val outputFiles  = collectOutputFiles()
    val dataFilesZip = workspace.resolve(DATA_ZIP_NAME)
      .also { Zip.compress(it, outputFiles) }

    inputDirectory.deleteRecursively()

    return workspace.resolve(OUTPUT_FILE_NAME)
      .also { Zip.compress(it, listOf(
        writeManifestFile(inputFiles, outputFiles),
        writeWarningFile(warnings), dataFilesZip),
        Zip.Level(0u)
      ) }
      .also { outputDirectory.deleteRecursively() }
  }

  override fun appendScriptEnv(env: MutableMap<String, String>) {
    super.appendScriptEnv(env)
    env[ScriptEnvKey.ImportID] = generateImportID()
  }

  @Suppress("NOTHING_TO_INLINE")
  private inline fun generateImportID() =
    Base36.encodeToString(OriginTimestamp.until(OffsetDateTime.now(), ChronoUnit.SECONDS).toULong()) +
      Base36.encodeToString(importCtx.request.importIndex.toULong())


  /**
   * Unpacks the given input archive into the input directory and ensures that
   * the archive contained at least one input file.
   *
   * @return A collection of [Pair]s containing the paths to the files in the
   * input directory paired with the sizes of those files.  The sizes are used
   * to build the `vdi-manifest.json` file.
   */
  private fun unpackInput(): Collection<Pair<Path, Long>> {
    Zip.zipEntries(importCtx.payload).forEach { (entry, inp) ->
      val file = inputDirectory.resolve(entry.name)
      file.outputStream().use { out -> inp.transferTo(out) }
    }

    importCtx.payload.deleteExisting()

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

    log.info("executing import script for VDI dataset ID {}", importCtx.request.vdiID)
    val warnings = executor.executeScript(
      script.path,
      workspace,
      arrayOf(inputDirectory.absolutePathString(), outputDirectory.absolutePathString()),
      buildScriptEnv(),
    ) {
      coroutineScope {
        val warnings = ArrayList<String>(8)

        val j1 = launch { LineListOutputStream(warnings).use { scriptStdOut.transferTo(it) } }
        val j2 = launch { LoggingOutputStream("[import][${importCtx.request.vdiID}]", log).use { scriptStdErr.transferTo(it) } }

        waitFor(script.maxSeconds)

        j1.join()
        j2.join()

        val importStatus = ExitStatus.Import.fromCode(exitCode())

        metrics.importScriptCalls.labelValues(importStatus.metricFriendlyName).inc()

        when (importStatus) {
          ExitStatus.Import.Success -> {
            val dur = timer.observeDuration()
            log.info("import script completed successfully for dataset {} in {} seconds", importCtx.request.vdiID, DoubleFmt.format(dur))
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
      .apply { outputStream().use { JSON.writeValue(it, importCtx.request.meta) } }

  private fun writeWarningFile(warnings: Collection<String>) =
    outputDirectory.resolve(WARNING_FILE_NAME)
      .createFile()
      .apply { outputStream().use { JSON.writeValue(it, WarningsFile(warnings)) } }

  private fun buildManifest(inputFiles: Collection<Pair<Path, Long>>, outputFiles: Collection<Path>) =
    VDIDatasetManifest(
      inputFiles = inputFiles.map { VDIDatasetFileInfo(it.first.name, it.second.toULong()) },
      dataFiles = outputFiles.map { VDIDatasetFileInfo(it.name, it.fileSize().toULong()) },
    )

  class EmptyInputError : RuntimeException("input archive contained no files")

  class ValidationError(val warnings: Collection<String>) : RuntimeException()

  data class WarningsFile(val warnings: Collection<String>)
}
