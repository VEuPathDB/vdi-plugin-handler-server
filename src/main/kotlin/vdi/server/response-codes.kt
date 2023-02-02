package vdi.server

import io.ktor.http.*

inline val StatusSuccess
  get() = HttpStatusCode.OK

inline val StatusNoContent
  get() = HttpStatusCode.NoContent

inline val StatusBadRequest
  get() = HttpStatusCode.BadRequest

inline val StatusValidationError
  get() = HttpStatusCode(418, "validation-error")

inline val StatusTransformationError
  get() = HttpStatusCode(420, "transformation-error")

inline val StatusServerError
  get() = HttpStatusCode.InternalServerError