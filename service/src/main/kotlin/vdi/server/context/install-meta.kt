package vdi.server.context

import com.fasterxml.jackson.core.JacksonException
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.veupathdb.vdi.lib.common.intra.InstallMetaRequest
import org.veupathdb.vdi.lib.json.JSON
import vdi.components.http.errors.BadRequestException
import vdi.components.http.errors.UnsupportedMediaTypeException
import vdi.components.io.BoundedInputStream
import vdi.util.withTempDirectory
import java.nio.file.Path

// Max allowed size of the post body: 32KiB
private const val MAX_INPUT_BYTES = 32768uL

suspend fun ApplicationCall.withInstallMetaContext(fn: suspend (workspace: Path, request: InstallMetaRequest) -> Unit) {
  if (!request.contentType().match(ContentType.Application.Json))
    throw UnsupportedMediaTypeException()

  val body = try {
    withContext(Dispatchers.IO) {
      JSON.readValue<InstallMetaRequest>(BoundedInputStream(this@withInstallMetaContext.receiveStream(), MAX_INPUT_BYTES))
    }
  } catch (e: JacksonException) {
    throw BadRequestException("Could not parse request body as JSON", e)
  }

  body.validate()

  withTempDirectory { workspace -> fn(workspace, body) }
}

private fun InstallMetaRequest.validate() {

  if (projectID.isBlank())
    throw BadRequestException("projectID cannot be blank")

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