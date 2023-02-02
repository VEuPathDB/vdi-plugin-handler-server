package vdi.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import vdi.util.toJSONString

suspend inline fun ApplicationCall.respondJSON400(body: Any) =
  respondJSON(body, StatusBadRequest)

suspend inline fun ApplicationCall.respondJSON418(body: Any) =
  respondJSON(body, StatusValidationError)

suspend inline fun ApplicationCall.respondJSON420(body: Any) =
  respondJSON(body, StatusTransformationError)

suspend inline fun ApplicationCall.respondJSON500(body: Any) =
  respondJSON(body, StatusServerError)

suspend inline fun ApplicationCall.respondJSON(body: Any, status: HttpStatusCode) =
  respondText(body.toJSONString(), ContentType.Application.Json, status)