package vdi.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import vdi.components.json.toJSONString

suspend inline fun ApplicationCall.respond204() =
  respond(StatusNoContent, Unit)


suspend inline fun ApplicationCall.respondJSON200(body: Any) =
  respondJSON(body, StatusSuccess)

suspend inline fun ApplicationCall.respondJSON400(body: Any) =
  respondJSON(body, StatusBadRequest)

suspend inline fun ApplicationCall.respondJSON418(body: Any) =
  respondJSON(body, StatusValidationError)

suspend inline fun ApplicationCall.respondJSON500(body: Any) =
  respondJSON(body, StatusServerError)

suspend inline fun ApplicationCall.respondJSON(body: Any, status: HttpStatusCode) =
  respondText(body.toJSONString(), ContentType.Application.Json, status)