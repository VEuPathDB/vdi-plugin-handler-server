package vdi.components.io

import java.io.OutputStream

private const val ASCII_LF = 0x0A

/**
 * Line List Output Stream
 *
 * An [OutputStream] implementation that collects written lines of text to the
 * given [MutableCollection] as they are written.
 *
 * @constructor Constructs a new `LineListOutputStream` instance.
 *
 * @param lines Collection into which written lines will be added.
 */
class LineListOutputStream(private val lines: MutableCollection<in String>) : OutputStream() {
  private val buffer = StringBuilder(1024)

  override fun write(b: Int) {
    if (b == ASCII_LF) {
      if (buffer.isNotBlank()) {
        lines.add(buffer.toString())
      }
      buffer.clear()
    } else {
      buffer.append(b.toChar())
    }
  }

  override fun close() {
    if (buffer.isNotBlank()) {
      lines.add(buffer.toString())
      buffer.clear()
    }
  }
}