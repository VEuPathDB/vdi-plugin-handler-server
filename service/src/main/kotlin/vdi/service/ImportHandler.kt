package vdi.service

import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import vdi.components.io.LineListOutputStream
import vdi.components.io.LoggingOutputStream
import vdi.components.json.JSON
import vdi.components.metrics.Metrics
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
  private val workspace: Path,
  private val inputFile: Path,
  private val details:   ImportDetails,
  private val executor:  ScriptExecutor,
  private val script:    ScriptConfiguration,
) {
  private val log = LoggerFactory.getLogger("ImportProcessor")

  private val inputDirectory: Path = workspace.resolve(INPUT_DIRECTORY_NAME)
    .createDirectory()

  private val outputDirectory: Path = workspace.resolve(OUTPUT_DIRECTORY_NAME)
    .createDirectory()

  @OptIn(ExperimentalPathApi::class)
  suspend fun processImport(): Path {
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
   * If the import script returns a status code of
   * [ExitCode.ImportScriptTransformationFailure], this method will throw a
   * [TransformationError] exception.
   *
   * If the import script returns any other status code, this method will throw
   * an [IllegalStateException].
   *
   * @return A collection of warnings raised by the import script during its
   * execution.
   */
  private suspend fun executeScript(): Collection<String> {
    val timer = Metrics.importScriptDuration.startTimer()

    val warnings = executor.executeScript(
      script.path,
      workspace,
      arrayOf(inputDirectory.absolutePathString(), outputDirectory.absolutePathString())
    ) {
      coroutineScope {
        val warnings = ArrayList<String>(8)

        val j1 = launch { LineListOutputStream(warnings).use { scriptStdOut.transferTo(it) } }
        val j2 = launch { LoggingOutputStream(log).use { scriptStdErr.transferTo(it) } }

        waitFor(script.maxSeconds)

        j1.join()
        j2.join()

        when (exitCode()) {
          ExitCode.ImportScriptSuccess
          -> {}

          ExitCode.ImportScriptValidationFailure
          -> throw ValidationError(warnings)

          ExitCode.ImportScriptTransformationFailure
          -> throw TransformationError(warnings)

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
      .apply { outputStream().use { JSON.writeValue(it, details.toMetaFile()) } }

  private fun writeWarningFile(warnings: Collection<String>) =
    outputDirectory.resolve(WARNING_FILE_NAME)
      .createFile()
      .apply { outputStream().use { JSON.writeValue(it, WarningsFile(warnings)) } }

  private fun ImportDetails.toMetaFile() =
    Meta(
      MetaType(type.name, type.version),
      projects,
      owner,
      name,
      if (summary.isNullOrBlank()) null else summary,
      if (description.isNullOrBlank()) null else description,
      dependencies.map { MetaDependency(it.resourceIdentifier, it.resourceVersion, it.resourceDisplayName) }
    )

  class EmptyInputError : RuntimeException("input archive contained no files")

  class ValidationError(val warnings: Collection<String>) : RuntimeException()

  class TransformationError(val warnings: Collection<String>) : RuntimeException()

  data class Manifest(val inputFiles: Collection<String>, val dataFiles: Collection<String>)

  data class Meta(
    val type: MetaType,
    val projects: Collection<String>,
    val owner: String,
    val name: String,
    val summary: String?,
    val description: String?,
    val dependencies: Collection<MetaDependency>
  )

  data class MetaType(
    val name:    String,
    val version: String,
  )

  data class MetaDependency(
    val resourceIdentifier:  String,
    val resourceVersion:     String,
    val resourceDisplayName: String
  )

  data class WarningsFile(val warnings: Collection<String>)
}