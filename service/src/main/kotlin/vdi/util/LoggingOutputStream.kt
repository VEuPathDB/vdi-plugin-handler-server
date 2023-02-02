package vdi.util

import org.slf4j.Logger
import java.io.OutputStream

private const val ASCII_LF = 0x0A

/**
 * Logging Output Stream
 *
 * An [OutputStream] implementation that writes lines of text to the given
 * [Logger] as they are written.
 *
 * All log lines are written to the logger via the [Logger.info] method.
 *
 * @constructor Constructs a new `LoggingOutputStream` instance.
 *
 * @param log `Logger` instance that the written bytes will be logged to.
 */
class LoggingOutputStream(private val log: Logger) : OutputStream() {
  private val buffer = StringBuilder(1024)

  override fun write(b: Int) {
    if (b == ASCII_LF) {
      log.info(buffer.toString())
      buffer.clear()
    } else {
      buffer.append(b.toChar())
    }
  }

  override fun close() {
    if (buffer.isNotEmpty()) {
      log.info(buffer.toString())
      buffer.clear()
    }
  }
}