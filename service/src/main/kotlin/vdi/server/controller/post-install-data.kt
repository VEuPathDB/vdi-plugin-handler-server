package vdi.server.controller

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.util.*
import io.ktor.utils.io.jvm.javaio.*
import java.nio.file.Path
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import vdi.Const
import vdi.components.http.errors.BadRequestException
import vdi.components.io.BoundedInputStream
import vdi.components.json.JSON
import vdi.components.ldap.LDAP
import vdi.components.script.ScriptExecutor
import vdi.conf.DatabaseConfigurationMap
import vdi.server.model.InstallDataSuccessResponse
import vdi.server.model.InstallDetails
import vdi.server.respondJSON200
import vdi.service.InstallDataService
import vdi.util.withTempDirectory

private const val INSTALL_PAYLOAD_FILE_NAME = "install.tar.gz"
private const val INSTALL_DETAILS_MAX_SIZE = 1024uL

class PostInstallDataController(
  private val ldap: LDAP,
  private val executor: ScriptExecutor,
  private val databases: DatabaseConfigurationMap,
) {

  suspend fun handlePostInstallData(call: ApplicationCall) {
    withTempDirectory { workspace ->
      val details: InstallDetails
      val payload: Path

      call.parseMultipartBody(workspace, { details = it }, { payload = it })
      details.validate()

      val dbEnvConfig = databases[details.projectID] ?: throw BadRequestException("unrecognized projectID value")

      val warnings = InstallDataService(workspace, details.vdiID, payload, ldap, executor, dbEnvConfig)
        .processInstall()

      call.respondJSON200(InstallDataSuccessResponse(warnings))
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
        Const.FieldName.Details -> {
          if (details)
            throw BadRequestException("part \"${Const.FieldName.Details}\" was specified more than once in the request body")

          part.parseInstallDetails(detailsCB)
          part.dispose()
          details = true
        }

        Const.FieldName.Payload -> {
          if (payload)
            throw BadRequestException("part \"${Const.FieldName.Payload}\" was specified more than once in the request body")

          part.handlePayload(workspace, payloadCB)
          part.dispose()
          payload = true
        }

        else -> {
          part.dispose()
          throw BadRequestException("unexpected part \"${part.name}\"")
        }
      }
    }

    details || throw BadRequestException("missing required part \"${Const.FieldName.Details}\"")
    payload || throw BadRequestException("missing required part \"${Const.FieldName.Payload}\"")
  }

  private fun PartData.parseInstallDetails(detailsCB: (InstallDetails) -> Unit) {
    when (this) {
      is PartData.BinaryChannelItem -> detailsCB(JSON.readValue(BoundedInputStream(provider().toInputStream(), INSTALL_DETAILS_MAX_SIZE)))
      is PartData.BinaryItem        -> detailsCB(JSON.readValue(BoundedInputStream(provider().asStream(), INSTALL_DETAILS_MAX_SIZE)))
      is PartData.FileItem          -> detailsCB(JSON.readValue(BoundedInputStream(streamProvider(), INSTALL_DETAILS_MAX_SIZE)))
      is PartData.FormItem          -> detailsCB(JSON.readValue(BoundedInputStream(value.byteInputStream(), INSTALL_DETAILS_MAX_SIZE)))
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
}
