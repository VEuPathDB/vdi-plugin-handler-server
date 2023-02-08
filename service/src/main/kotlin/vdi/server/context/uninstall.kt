package vdi.server.context

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import java.nio.file.Path
import vdi.components.http.errors.BadRequestException
import vdi.components.http.errors.UnsupportedMediaTypeException
import vdi.components.json.JSON
import vdi.consts.FieldName
import vdi.util.withTempDirectory

suspend fun ApplicationCall.withUninstallContext(
  fn: suspend (workspace: Path, vdiID: String, projectID: String) -> Unit
) {
  if (!request.contentType().match(ContentType.Application.Json))
    throw UnsupportedMediaTypeException()

  withTempDirectory { workspace ->
    val (vdiID, projectID) = parseBody()
    fn(workspace, vdiID, projectID)
  }
}

private suspend fun ApplicationCall.parseBody(): UninstallRequestBody {
  val out = receiveStream().use { JSON.readValue<UninstallRequestBody>(it) }

  out.vdiID.validateAsVDIID(FieldName.VDIID)

  if (out.projectID.isBlank())
    throw BadRequestException("projectID must not be blank")

  return out
}

@JsonIgnoreProperties(ignoreUnknown = true)
private data class UninstallRequestBody(
  @JsonProperty(FieldName.VDIID)
  val vdiID: String,

  @JsonProperty(FieldName.ProjectID)
  val projectID: String,
)