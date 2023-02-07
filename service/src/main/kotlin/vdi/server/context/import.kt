package vdi.server.context

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.*
import io.ktor.utils.io.jvm.javaio.*
import java.nio.file.Path
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import vdi.components.http.errors.BadRequestException
import vdi.components.io.BoundedInputStream
import vdi.components.json.JSON
import vdi.consts.FieldName
import vdi.server.model.ImportDetails
import vdi.util.withTempDirectory

private const val IMPORT_PAYLOAD_FILE_NAME = "import.tar.gz"
private const val IMPORT_DETAILS_MAX_SIZE  = 16384uL

suspend fun ApplicationCall.withImportContext(fn: suspend (workspace: Path, details: ImportDetails, payload: Path) -> Unit) {
  if (request.contentType() != ContentType.MultiPart.FormData)
    throw BadRequestException("invalid request content type")

  withTempDirectory { workspace ->
    val details: ImportDetails
    val payload: Path

    // Parse the body.
    parseMultipartBody(workspace, { details = it }, { payload = it })

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
  detailsCB: (ImportDetails) -> Unit,
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
private fun PartData.parseImportDetails(detailsCB: (ImportDetails) -> Unit) {
  when (this) {
    is PartData.BinaryChannelItem -> detailsCB(JSON.readValue(BoundedInputStream(provider().toInputStream(), IMPORT_DETAILS_MAX_SIZE)))
    is PartData.BinaryItem        -> detailsCB(JSON.readValue(BoundedInputStream(provider().asStream(), IMPORT_DETAILS_MAX_SIZE)))
    is PartData.FileItem          -> detailsCB(JSON.readValue(BoundedInputStream(streamProvider(), IMPORT_DETAILS_MAX_SIZE)))
    is PartData.FormItem          -> detailsCB(JSON.readValue(BoundedInputStream(value.byteInputStream(), IMPORT_DETAILS_MAX_SIZE)))
  }
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
private fun ImportDetails.validate() {
  vdiID.validateAsVDIID("vdiID")

  if (type.name.isBlank())
    throw BadRequestException("type.name cannot be blank")
  if (type.version.isBlank())
    throw BadRequestException("type.version cannot be blank")

  if (projects.isEmpty())
    throw BadRequestException("projects cannot be empty")
  for (project in projects)
    if (project.isBlank())
      throw BadRequestException("projects cannot contain a blank string")

  if (owner.isBlank())
    throw BadRequestException("owner cannot be blank")

  if (name.isBlank())
    throw BadRequestException("name cannot be blank")

  for (dependency in dependencies) {
    if (dependency.resourceIdentifier.isBlank())
      throw BadRequestException("dependency.resourceIdentifier cannot be blank")
    if (dependency.resourceVersion.isBlank())
      throw BadRequestException("dependency.resourceVersion cannot be blank")
    if (dependency.resourceDisplayName.isBlank())
      throw BadRequestException("dependency.resourceDisplayName cannot be blank")
  }
}
