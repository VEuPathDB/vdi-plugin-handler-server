package vdi.server.context

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.veupathdb.vdi.lib.common.intra.ImportRequest
import vdi.components.http.errors.BadRequestException
import vdi.components.http.errors.UnsupportedMediaTypeException
import vdi.consts.FieldName
import vdi.util.parseAsJson
import vdi.util.withTempDirectory
import java.nio.file.Path
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

private const val IMPORT_PAYLOAD_FILE_NAME = "import.zip"
private const val IMPORT_DETAILS_MAX_SIZE  = 16384uL

class ImportContext(
  val workspace: Path,
  val request: ImportRequest,
  val payload: Path,
) {
  override fun toString() = "ImportDataContext(datasetID: ${request.vdiID})"
}

suspend fun ApplicationCall.withImportContext(fn: suspend (importCtx: ImportContext) -> Unit) {
  if (!request.contentType().match(ContentType.MultiPart.FormData))
    throw UnsupportedMediaTypeException()

  withTempDirectory { workspace -> withContext(Dispatchers.IO) { withParsedRequest(workspace, fn) } }
}

@OptIn(ExperimentalContracts::class)
private suspend fun ApplicationCall.withParsedRequest(workspace: Path, fn: suspend (context: ImportContext) -> Unit) {
  contract { callsInPlace(fn, InvocationKind.EXACTLY_ONCE) }

  var details: ImportRequest? = null
  var payload: Path? = null

  receiveMultipart().forEachPart { part ->
    try {
      when (part.name) {
        FieldName.Details -> {
          reqNull(details, FieldName.Details)
          details = part.parseAsJson(IMPORT_DETAILS_MAX_SIZE)
        }

        FieldName.Payload -> {
          reqNull(payload, FieldName.Payload)
          payload = part.handlePayload(workspace, IMPORT_PAYLOAD_FILE_NAME)
        }
      }
    } finally {
      part.dispose()
    }
  }

  reqNotNull(details, FieldName.Details)
  reqNotNull(payload, FieldName.Payload)

  details!!.validate()

  fn(ImportContext(workspace, details!!, payload!!))
}

/**
 * Validates the posted details about a dataset being import processed.
 */
private fun ImportRequest.validate() {
  if (meta.type.name.toString().isBlank())
    throw BadRequestException("type.name cannot be blank")
  if (meta.type.version.isBlank())
    throw BadRequestException("type.version cannot be blank")

  if (meta.projects.isEmpty())
    throw BadRequestException("projects cannot be empty")
  for (project in meta.projects)
    if (project.isBlank())
      throw BadRequestException("projects cannot contain a blank string")

  if (meta.name.isBlank())
    throw BadRequestException("name cannot be blank")

  for (dependency in meta.dependencies) {
    if (dependency.identifier.isBlank())
      throw BadRequestException("dependency.resourceIdentifier cannot be blank")
    if (dependency.version.isBlank())
      throw BadRequestException("dependency.resourceVersion cannot be blank")
    if (dependency.displayName.isBlank())
      throw BadRequestException("dependency.resourceDisplayName cannot be blank")
  }
}
