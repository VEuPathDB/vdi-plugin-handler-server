package vdi.server.context

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import org.veupathdb.vdi.lib.common.intra.UninstallRequest
import org.veupathdb.vdi.lib.json.JSON
import vdi.components.http.errors.BadRequestException
import vdi.components.http.errors.UnsupportedMediaTypeException
import vdi.util.withTempDirectory
import java.nio.file.Path

suspend fun ApplicationCall.withUninstallContext(fn: suspend (workspace: Path, request: UninstallRequest) -> Unit) {
  if (!request.contentType().match(ContentType.Application.Json))
    throw UnsupportedMediaTypeException()

  withTempDirectory { workspace -> fn(workspace, parseBody()) }
}

private suspend fun ApplicationCall.parseBody(): UninstallRequest {
  val out = receiveStream().use { JSON.readValue<UninstallRequest>(it) }

  if (out.projectID.isBlank())
    throw BadRequestException("projectID must not be blank")

  return out
}
