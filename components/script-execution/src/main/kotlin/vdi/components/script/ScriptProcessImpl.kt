package vdi.components.script

import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

class ScriptProcessImpl(private val command: String, private val raw: Process) : ScriptProcess {
  override val scriptStdOut: InputStream
    get() = raw.inputStream

  override val scriptStdErr: InputStream
    get() = raw.errorStream

  override val scriptStdIn: OutputStream
    get() = raw.outputStream

  override fun waitFor(timeout: Duration) {
    if (!raw.waitFor(timeout.inWholeSeconds, TimeUnit.SECONDS)) {
      raw.destroyForcibly()
      throw IllegalStateException(
        "command " + command +
          " exceeded the configured maximum allowed execution time of " +
          timeout
      )
    }
  }

  override fun exitCode() = raw.exitValue()

  override fun isAlive() = raw.isAlive
}
