package vdi.util

import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import java.nio.file.Path
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.io.path.*

/**
 * Packs the target list of files into a `.tar.gz` file at the given path
 * ([into]).
 *
 * @param into Target file path to create the new tar archive at.
 *
 * @param overwrite Whether this function should overwrite any existing file at
 * the path [into].
 */
fun List<Path>.packAsTarGZ(into: Path, overwrite: Boolean = false) {
  if (into.exists() && !overwrite)
    throw IllegalStateException("unable to pack new archive $into as file already exists and overwrite is set to false")

  into.createFile()

  TarArchiveOutputStream(GZIPOutputStream(into.outputStream().buffered())).use { tar ->
    forEach { file ->
      tar.putArchiveEntry(TarArchiveEntry(file, file.name))
      file.inputStream().use { ins -> ins.transferTo(tar) }
      tar.closeArchiveEntry()
    }
  }
}

fun Path.unpackAsTarGZ(into: Path, overwrite: Boolean = false) {
  if (!into.exists())
    throw IllegalStateException("called with a non-existent target directory")
  if (!into.isDirectory())
    throw IllegalStateException("called with a non-directory target")

  TarArchiveInputStream(GZIPInputStream(inputStream().buffered())).use { tar ->
    tar.forEach { entry ->
      val target = into.resolve(entry.file.name)

      if (entry.isDirectory) {
        target.createDirectories()
      } else if (entry.isFile) {
        if (target.exists()) {
          if (target.isDirectory())
            throw IllegalStateException("unable to unpack the file $target due to a directory already existing at that path")
          if (!overwrite)
            throw IllegalStateException("unable to unpack the file $target due to a conflicting file already existing at that path while overwrite is set to false")
        }

        target.createFile()
        target.outputStream().use { os -> entry.file.inputStream().transferTo(os) }
      }
    }
  }
}

private inline fun TarArchiveInputStream.forEach(fn: (entry: TarArchiveEntry) -> Unit) {
  while (true)
    fn(nextTarEntry ?: break)
}