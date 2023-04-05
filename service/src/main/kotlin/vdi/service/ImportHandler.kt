package vdi.service

import org.slf4j.LoggerFactory
import org.veupathdb.vdi.lib.json.JSON
import java.nio.file.Path
import kotlin.io.path.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import vdi.components.io.LineListOutputStream
import vdi.components.io.LoggingOutputStream
import vdi.components.metrics.ScriptMetrics
import vdi.components.script.ScriptExecutor
import vdi.conf.ScriptConfiguration
import vdi.consts.ExitCode
import vdi.server.model.ImportDetails
import vdi.util.packAsTarGZ
import vdi.util.unpackAsTarGZ

private const val INPUT_DIRECTORY_NAME  = "input"
private const val OUTPUT_DIRECTORY_NAME = "output"
private const val MANIFEST_FILE_NAME    = "manifest.json"
private const val META_FILE_NAME        = "meta.json"
private const val WARNING_FILE_NAME     = "warnings.json"
private const val OUTPUT_FILE_NAME      = "output.tar.gz"

class ImportHandler(
  workspace: Path,
  private val inputFile: Path,
  private val details: ImportDetails,
  executor:  ScriptExecutor,
  private val script: ScriptConfiguration,
  metrics: ScriptMetrics,
) : HandlerBase<Path>(workspace, executor, metrics) {
  private val log = LoggerFactory.getLogger(javaClass)

  private val inputDirectory: Path = workspace.resolve(INPUT_DIRECTORY_NAME)
    .createDirectory()

  private val outputDirectory: Path = workspace.resolve(OUTPUT_DIRECTORY_NAME)
    .createDirectory()

  @OptIn(ExperimentalPathApi::class)
  override suspend fun run(): Path {
    val inputFiles = unpackInput()
    val warnings   = executeScript()

    inputDirectory.deleteRecursively()

    val outputFiles = collectOutputFiles()
      .apply {
        add(writeManifestFile(inputFiles, this))
        add(writeMetaFile())
        add(writeWarningFile(warnings))
      }

    return workspace.resolve(OUTPUT_FILE_NAME)
      .also { outputFiles.packAsTarGZ(it) }
      .also { outputDirectory.deleteRecursively() }
  }

  override fun buildScriptEnv(): Map<String, String> = emptyMap()

  /**
   * Unpacks the given input archive into the input directory and ensures that
   * the archive contained at least one input file.
   *
   * @return A collection of the names of the files that were unpacked into the
   * input directory.
   */
  private fun unpackInput(): Collection<String> {
    inputFile.unpackAsTarGZ(inputDirectory)
    inputFile.deleteExisting()

    val inputFiles = inputDirectory.listDirectoryEntries()
      .map { it.name }

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
   * [ExitCode.ImportScriptSuccess], this method returns normally.
   *
   * If the import script returns a status code of
   * [ExitCode.ImportScriptValidationFailure], this method will throw a
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

        when (exitCode()) {
          ExitCode.ImportScriptSuccess
          -> {}

          ExitCode.ImportScriptValidationFailure
          -> throw ValidationError(warnings)

          else
          -> throw IllegalStateException("import script failed with unexpected exit code")
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

  private fun writeManifestFile(inputFiles: Collection<String>, outputFiles: Collection<Path>) =
    outputDirectory.resolve(MANIFEST_FILE_NAME)
      .createFile()
      .apply { outputStream().use { JSON.writeValue(it, Manifest(inputFiles, outputFiles.map { it.name })) } }

  private fun writeMetaFile() =
    outputDirectory.resolve(META_FILE_NAME)
      .createFile()
      .apply { outputStream().use { JSON.writeValue(it, details.meta) } }

  private fun writeWarningFile(warnings: Collection<String>) =
    outputDirectory.resolve(WARNING_FILE_NAME)
      .createFile()
      .apply { outputStream().use { JSON.writeValue(it, WarningsFile(warnings)) } }

  class EmptyInputError : RuntimeException("input archive contained no files")

  class ValidationError(val warnings: Collection<String>) : RuntimeException()

  data class Manifest(val inputFiles: Collection<String>, val dataFiles: Collection<String>)

  data class WarningsFile(val warnings: Collection<String>)
}