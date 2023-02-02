package vdi.components.http.errors

// 415
class UnsupportedMediaTypeException(message: String = "unsupported Content-Type") : RuntimeException(message)