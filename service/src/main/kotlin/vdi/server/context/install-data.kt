package vdi.server.context

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
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
import vdi.server.model.InstallDetails
import vdi.util.withTempDirectory

private const val INSTALL_PAYLOAD_FILE_NAME = "install.tar.gz"
private const val INSTALL_DETAILS_MAX_SIZE = 1024uL

suspend fun ApplicationCall.withInstallDataContext(fn: suspend (workspace: Path, details: InstallDetails, payload: Path) -> Unit) {
  if (request.contentType() != ContentType.MultiPart.FormData)
    throw BadRequestException("invalid request content type")

  // Create temp directory
  withTempDirectory { workspace ->
    val details: InstallDetails
    val payload: Path

    // Parse the multipart post body
    parseMultipartBody(workspace, { details = it }, { payload = it })

    // Validate the details
    details.validate()

    fn(workspace, details, payload)
  }
}

@OptIn(ExperimentalContracts::class)
private suspend fun ApplicationCall.parseMultipartBody(
  workspace: Path,
  detailsCB: (InstallDetails) -> Unit,
  payloadCB: (Path) -> Unit,
) {
  contract {
    callsInPlace(detailsCB, InvocationKind.EXACTLY_ONCE)
    callsInPlace(payloadCB, InvocationKind.EXACTLY_ONCE)
  }

  var details = false
  var payload = false

  receiveMultipart().forEachPart { part ->
    when (part.name) {
      FieldName.Details -> {
        if (details)
          throw BadRequestException("part \"${FieldName.Details}\" was specified more than once in the request body")

        part.parseInstallDetails(detailsCB)
        part.dispose()
        details = true
      }

      FieldName.Payload -> {
        if (payload)
          throw BadRequestException("part \"${FieldName.Payload}\" was specified more than once in the request body")

        part.handlePayload(workspace, payloadCB)
        part.dispose()
        payload = true
      }

      else                    -> {
        part.dispose()
        throw BadRequestException("unexpected part \"${part.name}\"")
      }
    }
  }

  details || throw BadRequestException("missing required part \"${FieldName.Details}\"")
  payload || throw BadRequestException("missing required part \"${FieldName.Payload}\"")
}

private fun PartData.parseInstallDetails(detailsCB: (InstallDetails) -> Unit) {
  when (this) {
    is PartData.BinaryChannelItem -> detailsCB(
      JSON.readValue(
        BoundedInputStream(
          provider().toInputStream(),
          INSTALL_DETAILS_MAX_SIZE
        )
      ))
    is PartData.BinaryItem        -> detailsCB(
      JSON.readValue(
        BoundedInputStream(
          provider().asStream(),
          INSTALL_DETAILS_MAX_SIZE
        )
      ))
    is PartData.FileItem          -> detailsCB(
      JSON.readValue(
        BoundedInputStream(
          streamProvider(),
          INSTALL_DETAILS_MAX_SIZE
        )
      ))
    is PartData.FormItem          -> detailsCB(
      JSON.readValue(
        BoundedInputStream(
          value.byteInputStream(),
          INSTALL_DETAILS_MAX_SIZE
        )
      ))
  }
}

private fun PartData.handlePayload(workspace: Path, payloadCB: (Path) -> Unit) {
  handlePayload(workspace, INSTALL_PAYLOAD_FILE_NAME, payloadCB)
}

private fun InstallDetails.validate() {
  vdiID.validateAsVDIID("vdiID")

  if (projectID.isBlank()) {
    throw BadRequestException("projectID cannot be a blank value")
  }
}
