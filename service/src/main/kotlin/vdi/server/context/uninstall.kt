package vdi.server.context

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import org.veupathdb.vdi.lib.common.field.DatasetID
import org.veupathdb.vdi.lib.common.field.ProjectID
import org.veupathdb.vdi.lib.json.JSON
import java.nio.file.Path
import vdi.components.http.errors.BadRequestException
import vdi.components.http.errors.UnsupportedMediaTypeException
import vdi.consts.FieldName
import vdi.util.withTempDirectory

suspend fun ApplicationCall.withUninstallContext(
  fn: suspend (workspace: Path, vdiID: DatasetID, projectID: String) -> Unit
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

  if (out.projectID.isBlank())
    throw BadRequestException("projectID must not be blank")

  return out
}

@JsonIgnoreProperties(ignoreUnknown = true)
private data class UninstallRequestBody(
  @JsonProperty(FieldName.VDIID)
  val vdiID: DatasetID,

  @JsonProperty(FieldName.ProjectID)
  val projectID: ProjectID,
)