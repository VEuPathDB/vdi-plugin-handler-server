package vdi.components.http.errors

// 400
class BadRequestException : RuntimeException {
  constructor(message: String) : super(message)
  constructor(cause: Throwable) : super(cause)
}