package vdi.util

import org.veupathdb.vdi.lib.common.compression.Zip
import java.nio.file.Path
import kotlin.io.path.*


internal fun Path.unpackAsZip(into: Path) {
  Zip.zipEntries(this).forEach { (entry, stream) ->
    into.resolve(entry.name)
      .createFile()
      .outputStream()
      .buffered()
      .use { stream.buffered().transferTo(it) }
  }
}
