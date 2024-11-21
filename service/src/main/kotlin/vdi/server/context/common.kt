package vdi.server.context

import io.ktor.http.content.*
import io.ktor.util.*
import io.ktor.utils.io.jvm.javaio.*
import vdi.components.http.errors.BadRequestException
import java.nio.file.Path
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.io.path.createFile
import kotlin.io.path.outputStream

internal fun PartData.handlePayload(workspace: Path, fileName: String): Path {
  val payload = workspace.resolve(fileName)

  payload.createFile()
  payload.outputStream().use {
    when (this) {
      is PartData.BinaryChannelItem -> provider().toInputStream().transferTo(it)
      is PartData.BinaryItem        -> provider().asStream().transferTo(it)
      is PartData.FileItem          -> provider().toInputStream().transferTo(it)
      is PartData.FormItem          -> value.byteInputStream().transferTo(it)
    }
  }

  return payload
}

@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalContracts::class)
internal inline fun reqNull(obj: Any?, key: String) {
  contract { returns() implies (obj == null) }
  if (obj != null)
    throw BadRequestException("form part \"$key\" was specified more than once in the request body")
}

@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalContracts::class)
internal inline fun reqNotNull(obj: Any?, key: String) {
  contract { returns() implies (obj != null) }
  if (obj == null)
    throw BadRequestException("missing required form part \"$key\"")
}
