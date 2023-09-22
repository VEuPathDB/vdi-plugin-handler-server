package vdi.server.context

import io.ktor.http.content.*
import io.ktor.util.*
import io.ktor.utils.io.jvm.javaio.*
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.outputStream
import vdi.components.http.errors.BadRequestException

fun PartData.handlePayload(workspace: Path, fileName: String, payloadCB: (Path) -> Unit) {
  val payload = workspace.resolve(fileName)

  payload.createFile()
  payload.outputStream().use {
    when (this) {
      is PartData.BinaryChannelItem -> provider().toInputStream().transferTo(it)
      is PartData.BinaryItem        -> provider().asStream().transferTo(it)
      is PartData.FileItem          -> streamProvider().transferTo(it)
      is PartData.FormItem          -> value.byteInputStream().transferTo(it)
    }
  }

  payloadCB(payload)
}
