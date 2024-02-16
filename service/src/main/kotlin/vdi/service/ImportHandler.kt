package vdi.service

import org.slf4j.LoggerFactory
import org.veupathdb.vdi.lib.json.JSON
import java.nio.file.Path
import kotlin.io.path.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.veupathdb.vdi.lib.common.DatasetManifestFilename
import org.veupathdb.vdi.lib.common.DatasetMetaFilename
import org.veupathdb.vdi.lib.common.compression.Zip
import org.veupathdb.vdi.lib.common.model.VDIDatasetFileInfoImpl
import org.veupathdb.vdi.lib.common.model.VDIDatasetManifestImpl
import vdi.components.io.LineListOutputStream
import vdi.components.io.LoggingOutputStream
import vdi.components.metrics.ScriptMetrics
import vdi.components.script.ScriptExecutor
import vdi.conf.ScriptConfiguration
import vdi.consts.ExitStatus
import vdi.server.model.ImportDetails
import vdi.util.packAsTarGZ

private const val INPUT_DIRECTORY_NAME  = "input"
private const val OUTPUT_DIRECTORY_NAME = "output"
private const val WARNING_FILE_NAME     = "warnings.json"
private const val OUTPUT_FILE_NAME      = "output.tar.gz"

class ImportHandler(
  workspace: Path,
  private val inputFile: Path,
  private val details: ImportDetails,
  executor:  ScriptExecutor,
  private val script: ScriptConfiguration,
  customPath: String,
  metrics: ScriptMetrics,
) : HandlerBase<Path>(workspace, executor, customPath, metrics) {
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
      .also { outputFiles.packAsTarGZ(it) }
      .also { outputDirectory.deleteRecursively() }
  }

  /**
   * Unpacks the given input archive into the input directory and ensures that
   * the archive contained at least one input file.
   *
   * @return A collection of the names of the files that were unpacked into the
   * input directory.
   */
  private fun unpackInput(): Collection<Path> {
    Zip.zipEntries(inputFile).forEach { (entry, inp) ->
      val file = inputDirectory.resolve(entry.name)
      file.outputStream().use { out -> inp.transferTo(out) }
    }
    inputFile.deleteExisting()

    val inputFiles = inputDirectory.listDirectoryEntries()

    if (inputFiles.isEmpty())
      throw EmptyInputError()

    return inputFiles
  }

  /**
   * Executes the import script.
   *
   * If the import script fails execution this method will raise an appropriate
   * exception.
   *
   * If the import script returns a status code of
   * [ExitStatus.ImportScriptSuccess], this method returns normally.
   *
   * If the import script returns a status code of
   * [ExitStatus.ImportScriptValidationFailure], this method will throw a
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
          ExitStatus.Import.Success
          -> {}

          ExitStatus.Import.ValidationFailure
          -> throw ValidationError(warnings)

          else
          -> throw IllegalStateException("import script failed with unexpected exit code ${exitCode()}")
        }

        warnings
      }
    }

    timer.observeDuration()

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

  private fun writeManifestFile(inputFiles: Collection<Path>, outputFiles: Collection<Path>) =
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

  private fun buildManifest(inputFiles: Collection<Path>, outputFiles: Collection<Path>) =
    VDIDatasetManifestImpl(
      inputFiles = inputFiles.map { VDIDatasetFileInfoImpl(it.name, it.fileSize()) },
      dataFiles = outputFiles.map { VDIDatasetFileInfoImpl(it.name, it.fileSize()) },
    )

  class EmptyInputError : RuntimeException("input archive contained no files")

  class ValidationError(val warnings: Collection<String>) : RuntimeException()

  data class Manifest(val inputFiles: Collection<String>, val dataFiles: Collection<String>)

  data class WarningsFile(val warnings: Collection<String>)
}