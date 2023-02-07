package vdi.server.controller

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlin.io.path.inputStream
import vdi.components.http.errors.SimpleErrorResponse
import vdi.components.script.ScriptExecutor
import vdi.conf.ScriptConfiguration
import vdi.server.context.withImportContext
import vdi.server.model.WarningsListResponse
import vdi.server.respondJSON400
import vdi.server.respondJSON418
import vdi.server.respondJSON420
import vdi.service.ImportHandler

suspend fun ApplicationCall.handleImportRequest(executor: ScriptExecutor, script: ScriptConfiguration) {
  withImportContext { workspace, details, payload ->
    try {
      val outFile = ImportHandler(workspace, payload, details, executor, script).processImport()

      respondOutputStream(ContentType.Application.OctetStream, HttpStatusCode.OK) {
        outFile.inputStream().use { it.transferTo(this) }
      }
    } catch (e: ImportHandler.ValidationError) {
      respondJSON418(WarningsListResponse(e.warnings))
    } catch (e: ImportHandler.TransformationError) {
      respondJSON420(WarningsListResponse(e.warnings))
    } catch (e: ImportHandler.EmptyInputError) {
      respondJSON400(SimpleErrorResponse(e.message!!))
    }
  }
}
