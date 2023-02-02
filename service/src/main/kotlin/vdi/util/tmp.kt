package vdi.util

import java.io.File
import java.util.UUID
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract


fun makeTempDirectory() : File {
  var tmpDir: File

  while (true) {
    val uuid = UUID.randomUUID().toString()
    tmpDir = File("/tmp/$uuid")

    if (tmpDir.exists())
      continue
    else
      break
  }

  tmpDir.mkdir()

  return tmpDir
}

@OptIn(ExperimentalContracts::class)
inline fun withTempDirectory(fn: (tempDir: File) -> Unit) {
  contract { callsInPlace(fn, kotlin.contracts.InvocationKind.EXACTLY_ONCE) }

  val tempDir = makeTempDirectory()
  try {
    fn(tempDir)
  } finally {
    tempDir.deleteRecursively()
  }
}