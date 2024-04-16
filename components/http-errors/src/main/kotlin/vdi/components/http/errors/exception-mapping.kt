package vdi.components.http.errors

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import org.slf4j.LoggerFactory
import org.veupathdb.vdi.lib.common.intra.SimpleErrorResponse
import org.veupathdb.vdi.lib.json.toJSONString

private val log = LoggerFactory.getLogger("ExceptionMiddleware")

suspend fun PipelineContext<*, ApplicationCall>.withExceptionMapping(
  fn: suspend PipelineContext<*, ApplicationCall>.() -> Unit
) {
  try {
    fn()
  } catch (e: Throwable) {
    when (e) {
      is BadRequestException -> {
        log.debug("Thrown 400 exception.", e)
        call.respondText(
          SimpleErrorResponse(e.message ?: "null").toJSONString(),
          ContentType.Application.Json,
          HttpStatusCode.BadRequest,
        )
      }

      is UnsupportedMediaTypeException -> {
        log.debug("Thrown 415 exception.", e)
        call.respondText(
          SimpleErrorResponse(e.message ?: "null").toJSONString(),
          ContentType.Application.Json,
          HttpStatusCode.UnsupportedMediaType,
        )
      }

      else -> {
        log.error("Uncaught exception", e)
        call.respondText(
          SimpleErrorResponse(e.message ?: "null").toJSONString(),
          ContentType.Application.Json,
          HttpStatusCode.InternalServerError,
        )
      }
    }
  }
}
