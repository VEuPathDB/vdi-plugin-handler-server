package vdi.components.script

import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

class ScriptProcessImpl(private val command: String, private val raw: Process) : ScriptProcess {
  override val scriptStdOut: InputStream
    get() = raw.inputStream

  override val scriptStdErr: InputStream
    get() = raw.errorStream

  override val scriptStdIn: OutputStream
    get() = raw.outputStream

  override fun waitFor(timeoutSeconds: Long) {
    if (timeoutSeconds > -1) {
      if (!raw.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
        raw.destroyForcibly()
        throw IllegalStateException(
          "command " + command +
            " exceeded the configured maximum allowed execution time of " +
            timeoutSeconds.seconds
        )
      }
    } else {
      raw.waitFor()
    }
  }

  override fun exitCode() = raw.exitValue()

  override fun isAlive() = raw.isAlive
}