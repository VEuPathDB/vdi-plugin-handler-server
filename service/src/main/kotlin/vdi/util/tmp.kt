package vdi.util

import java.nio.file.Path
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createDirectory
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists


fun makeTempDirectory(): Path {
  var tmpDir: Path

  while (true) {
    val uuid = UUID.randomUUID().toString()
    tmpDir = Path.of("tmp", uuid)

    if (tmpDir.exists())
      continue
    else
      break
  }

  tmpDir.createDirectory()

  return tmpDir
}

@OptIn(ExperimentalContracts::class, ExperimentalPathApi::class)
inline fun withTempDirectory(fn: (tempDir: Path) -> Unit) {
  contract { callsInPlace(fn, kotlin.contracts.InvocationKind.EXACTLY_ONCE) }

  val tempDir = makeTempDirectory()
  try {
    fn(tempDir)
  } finally {
    tempDir.deleteRecursively()
  }
}