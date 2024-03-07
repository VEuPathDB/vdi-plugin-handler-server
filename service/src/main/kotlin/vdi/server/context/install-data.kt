package vdi.server.context

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.util.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.veupathdb.vdi.lib.json.JSON
import java.nio.file.Path
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import vdi.components.http.errors.BadRequestException
import vdi.components.http.errors.UnsupportedMediaTypeException
import vdi.components.io.BoundedInputStream
import vdi.consts.FieldName
import vdi.server.model.InstallDetails
import vdi.util.parseAsJson
import vdi.util.withTempDirectory

private const val INSTALL_PAYLOAD_FILE_NAME = "install-ready.zip"
private const val INSTALL_DETAILS_MAX_SIZE = 1024uL

suspend fun ApplicationCall.withInstallDataContext(fn: suspend (workspace: Path, details: InstallDetails, payload: Path) -> Unit) {
  if (!request.contentType().match(ContentType.MultiPart.FormData))
    throw UnsupportedMediaTypeException()

  // Create temp directory
  withTempDirectory { workspace ->
    val details: InstallDetails
    val payload: Path

    // Parse the multipart post body
    withContext(Dispatchers.IO) {
      parseMultipartBody(workspace, { details = it }, { payload = it })
    }

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
    try {
      when (part.name) {
        FieldName.Details -> {
          if (details)
            throw BadRequestException("part \"${FieldName.Details}\" was specified more than once in the request body")

          part.parseInstallDetails(detailsCB)
          details = true
        }

        FieldName.Payload -> {
          if (payload)
            throw BadRequestException("part \"${FieldName.Payload}\" was specified more than once in the request body")

          part.handlePayload(workspace, payloadCB)
          payload = true
        }

        else -> {
          throw BadRequestException("unexpected part \"${part.name}\"")
        }
      }
    } finally {
      part.dispose()
    }
  }

  details || throw BadRequestException("missing required part \"${FieldName.Details}\"")
  payload || throw BadRequestException("missing required part \"${FieldName.Payload}\"")
}

private fun PartData.parseInstallDetails(detailsCB: (InstallDetails) -> Unit) {
  detailsCB(parseAsJson(INSTALL_DETAILS_MAX_SIZE, InstallDetails::class))
}

private fun PartData.handlePayload(workspace: Path, payloadCB: (Path) -> Unit) {
  handlePayload(workspace, INSTALL_PAYLOAD_FILE_NAME, payloadCB)
}

private fun InstallDetails.validate() {
  if (projectID.isBlank()) {
    throw BadRequestException("projectID cannot be a blank value")
  }
}
