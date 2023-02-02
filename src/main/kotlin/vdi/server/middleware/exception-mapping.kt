package vdi.server.middleware

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*
import org.slf4j.LoggerFactory
import vdi.server.model.SimpleErrorResponse
import vdi.util.toJSONString

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
          SimpleErrorResponse(e.message!!).toJSONString(),
          ContentType.Application.Json,
          HttpStatusCode.BadRequest,
        )
      }

      is NotFoundException -> {
        log.debug("Thrown 404 exception.", e)
        call.respondText(
          SimpleErrorResponse(e.message!!).toJSONString(),
          ContentType.Application.Json,
          HttpStatusCode.NotFound,
        )
      }

      is UnsupportedMediaTypeException -> {
        log.debug("Thrown 415 exception.", e)
        call.respondText(
          SimpleErrorResponse(e.message!!).toJSONString(),
          ContentType.Application.Json,
          HttpStatusCode.UnsupportedMediaType,
        )
      }

      is InternalServerException -> {
        log.warn("Thrown 500 exception.", e)
        call.respondText(
          SimpleErrorResponse(e.message!!).toJSONString(),
          ContentType.Application.Json,
          HttpStatusCode.InternalServerError,
        )
      }

      else -> {
        log.error("Uncaught exception", e)
        call.respondText(
          SimpleErrorResponse(e.message!!).toJSONString(),
          ContentType.Application.Json,
          HttpStatusCode.InternalServerError,
        )
      }
    }
  }
}

// 400
class BadRequestException : RuntimeException {
  constructor(message: String) : super(message)
  constructor(cause: Throwable) : super(cause)
}

// 404
class NotFoundException : RuntimeException("resource not found")

// 415
class UnsupportedMediaTypeException(message: String = "unsupported Content-Type") : RuntimeException(message)

// 500
class InternalServerException(message: String) : RuntimeException(message)