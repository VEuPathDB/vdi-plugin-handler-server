package vdi.server.controller

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory
import org.veupathdb.vdi.lib.common.intra.SimpleErrorResponse
import org.veupathdb.vdi.lib.common.intra.WarningResponse
import kotlin.io.path.inputStream
import vdi.model.ApplicationContext
import vdi.server.context.withImportContext
import vdi.server.respondJSON400
import vdi.server.respondJSON418
import vdi.service.ImportHandler

private val log = LoggerFactory.getLogger("import-controller")

suspend fun ApplicationCall.handleImportRequest(appCtx: ApplicationContext) {
  withImportContext { workspace, request, payload ->
    try {
      val outFile = ImportHandler(
        workspace,
        payload,
        request,
        appCtx.executor,
        appCtx.config.service.importScript,
        appCtx.config.service.customPath,
        appCtx.metrics.scriptMetrics,
      )
        .run()

      log.debug("sending import response body for dataset {}", request.vdiID)
      respondOutputStream(ContentType.Application.OctetStream, HttpStatusCode.OK) {
        outFile.inputStream().use { it.transferTo(this) }
      }
    } catch (e: ImportHandler.ValidationError) {
      respondJSON418(WarningResponse(e.warnings))
    } catch (e: ImportHandler.EmptyInputError) {
      respondJSON400(SimpleErrorResponse(e.message!!))
    }
  }
}
