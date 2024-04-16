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

suspend fun ApplicationCall.withImportContext(fn: suspend (workspace: Path, details: ImportRequest, payload: Path) -> Unit) {
  if (!request.contentType().match(ContentType.MultiPart.FormData))
    throw UnsupportedMediaTypeException()

  withTempDirectory { workspace ->
    val details: ImportRequest
    val payload: Path

    // Parse the body.
    withContext(Dispatchers.IO) {
      parseMultipartBody(workspace, { details = it }, { payload = it })
    }

    // Validate the details JSON
    details.validate()

    fn(workspace, details, payload)
  }
}
/**
 * Parses the expected fields out of the `multipart/form-data` POST request
 * body.
 *
 * @param workspace Workspace directory path into which the payload from the
 * POST request will be written.
 *
 * @param detailsCB Callback that will be passed the `ImportDetails` instance
 * once it is parsed from the request body.
 *
 * @param payloadCB Callback that will be passed the payload `Path` instance
 * once it is copied from the request body to the workspace.
 */
@OptIn(ExperimentalContracts::class)
private suspend fun ApplicationCall.parseMultipartBody(
  workspace: Path,
  detailsCB: (ImportRequest) -> Unit,
  payloadCB: (Path) -> Unit,
) {
  contract {
    callsInPlace(detailsCB, InvocationKind.EXACTLY_ONCE)
    callsInPlace(payloadCB, InvocationKind.EXACTLY_ONCE)
  }

  var details = false
  var payload = false

  receiveMultipart().forEachPart {
    when (it.name) {
      FieldName.Details -> {
        if (details)
          throw BadRequestException("part \"${FieldName.Details}\" was specified more than once in the request body")

        it.parseImportDetails(detailsCB)
        details = true
      }

      FieldName.Payload -> {
        if (payload)
          throw BadRequestException("part \"${FieldName.Payload}\" was specified more than once in the request body")

        it.handlePayload(workspace, payloadCB)
        payload = true
      }

      else -> throw BadRequestException("unexpected part \"${it.name}\"")
    }
  }

  details || throw BadRequestException("missing required part \"${FieldName.Details}\"")
  payload || throw BadRequestException("missing required part \"${FieldName.Payload}\"")
}

/**
 * Parses the `"details"` part/field in the `multipart/form-data` POST request
 * body.
 *
 * Additionally, as this JSON blob is loaded into memory (if valid), this method
 * enforces that the `"details"` part of the POST body does not exceed the set
 * [IMPORT_DETAILS_MAX_SIZE] value.
 *
 * @param detailsCB Callback that will be called with an `ImportDetails`
 * instance parsed from the target `PartData`.
 */
private fun PartData.parseImportDetails(detailsCB: (ImportRequest) -> Unit) {
  detailsCB(parseAsJson(IMPORT_DETAILS_MAX_SIZE, ImportRequest::class))
}

/**
 * Copies the contents of the `"payload"` part in the `multipart/form-data` POST
 * request body into a new file in the given [workspace] directory.
 *
 * @param workspace Workspace directory in which the contents of the `"payload"`
 * part of the request body will be copied.
 *
 * @param payloadCB Call back that will be called with a `Path` handle on the
 * file created in the given [workspace] for the `"payload"` contents copied
 * from the request body.
 */
private fun PartData.handlePayload(workspace: Path, payloadCB: (Path) -> Unit) {
  handlePayload(workspace, IMPORT_PAYLOAD_FILE_NAME, payloadCB)
}

/**
 * Validates the posted details about a dataset being import processed.
 */
private fun ImportRequest.validate() {
  if (meta.type.name.isBlank())
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
