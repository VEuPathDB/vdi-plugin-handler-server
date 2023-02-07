package vdi.components.http.errors

// 500
class InternalServerException : RuntimeException {

  constructor(message: String) : super(message)

  constructor(message: String, cause: Throwable) : super(message, cause)
}