package vdi.server.context

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.veupathdb.vdi.lib.common.intra.InstallDataRequest
import org.veupathdb.vdi.lib.common.model.VDIDatasetManifest
import org.veupathdb.vdi.lib.common.model.VDIDatasetMeta
import vdi.components.http.errors.BadRequestException
import vdi.components.http.errors.UnsupportedMediaTypeException
import vdi.consts.FieldName
import vdi.util.parseAsJson
import vdi.util.withTempDirectory
import java.nio.file.Path
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

private const val INSTALL_PAYLOAD_FILE_NAME = "install-ready.zip"
private const val INSTALL_DETAILS_MAX_SIZE = 1024uL

class InstallDataContext(
  val workspace: Path,
  val request: InstallDataRequest,
  val payload: Path,
  val meta: VDIDatasetMeta,
  val manifest: VDIDatasetManifest,
) {
  override fun toString() = "InstallDataContext(datasetID: ${request.vdiID}, projectID: ${request.projectID})"
}

suspend fun ApplicationCall.withInstallDataContext(fn: suspend (installCtx: InstallDataContext) -> Unit) {
  if (!request.contentType().match(ContentType.MultiPart.FormData))
    throw UnsupportedMediaTypeException()

  withTempDirectory { workspace -> withContext(Dispatchers.IO) { withParsedRequest(workspace, fn) } }
}

@OptIn(ExperimentalContracts::class)
private suspend fun ApplicationCall.withParsedRequest(workspace: Path, fn: suspend (context: InstallDataContext) -> Unit) {
  contract { callsInPlace(fn, InvocationKind.EXACTLY_ONCE) }

  var details: InstallDataRequest? = null
  var payload: Path? = null
  var meta: VDIDatasetMeta? = null
  var manifest: VDIDatasetManifest? = null

  receiveMultipart().forEachPart { part ->
    try {
      when (part.name) {
        FieldName.Details -> {
          reqNull(details, FieldName.Details)
          details = part.parseAsJson<InstallDataRequest>(INSTALL_DETAILS_MAX_SIZE)
        }

        FieldName.Metadata -> {
          reqNull(meta, FieldName.Metadata)
          meta = part.parseAsJson<VDIDatasetMeta>(16384uL)
        }

        FieldName.Manifest -> {
          reqNull(manifest, FieldName.Manifest)
          manifest = part.parseAsJson<VDIDatasetManifest>(16384uL)
        }

        FieldName.Payload -> {
          reqNull(payload, FieldName.Payload)
          payload = part.handlePayload(workspace, INSTALL_PAYLOAD_FILE_NAME)
        }
      }
    } finally {
      part.dispose()
    }
  }

  reqNotNull(details, FieldName.Details)
  reqNotNull(meta, FieldName.Metadata)
  reqNotNull(manifest, FieldName.Manifest)
  reqNotNull(payload, FieldName.Payload)

  details!!.validate()

  fn(InstallDataContext(workspace, details!!, payload!!, meta!!, manifest!!))
}

private fun InstallDataRequest.validate() {
  if (projectID.isBlank()) {
    throw BadRequestException("projectID cannot be a blank value")
  }
}
