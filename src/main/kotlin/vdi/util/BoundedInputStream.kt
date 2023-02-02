package vdi.util

import java.io.IOException
import java.io.InputStream

/**
 * Bounded Input Stream
 *
 * An [InputStream] wrapper that applies a given [limit] to the maximum number
 * of bytes that can be read from the source stream.  If the number of bytes
 * read from the source stream exceeds the configured [limit], an exception will
 * be thrown.  This exception can be customized via a given exception provider
 * function.
 *
 * @author Elizabeth Paige Harper - https://github.com/foxcapades
 * @since 1.0.0
 */
class BoundedInputStream : InputStream {

  /**
   * Original/underlying stream.
   */
  private val stream: InputStream

  /**
   * Error Provider Function
   *
   * This function will be called when the configured [limit] of bytes read is
   * exceeded to provide the exception that will be thrown in that case.
   */
  private val errorProvider: () -> Throwable

  /**
   * Byte Read Limit
   *
   * Maximum number of bytes that is legal to read from the underlying stream.
   *
   * If this limit is exceeded by reading a number of bytes from this stream
   * that is greater than this limit, an exception will be thrown.
   */
  val limit: ULong

  /**
   * Current Number of Bytes Read
   *
   * A running total of the number of bytes that have been read from the
   * underlying stream.
   */
  var bytesRead: ULong = 0uL
    private set

  /**
   * Constructs a new `BoundedInputStream` instance.
   *
   * @param stream The target `InputStream` to wrap and apply the limit to.
   *
   * @param limit Maximum number of bytes that may be read from the underlying
   * [stream] before an exception will be thrown.
   *
   * @param errorProvider Function that will be used to provide the exception
   * that is thrown when the maximum byte [limit] is exceeded.
   */
  constructor(
    stream: InputStream,
    limit: ULong,
    errorProvider: () -> Throwable = { IOException("input stream exceeded max length of $limit") }
  ) {
    this.stream = stream
    this.limit = limit
    this.errorProvider = errorProvider
  }

  override fun read(): Int {
    val out = stream.read()

    if (out > -1)
      increaseBytesRead(1u)

    return out
  }

  override fun read(b: ByteArray): Int {
    val red = stream.read(b)

    if (red > 0)
      increaseBytesRead(red.toULong())

    return red
  }

  override fun read(b: ByteArray, off: Int, len: Int): Int {
    val red = stream.read(b, off, len)

    if (red > 0)
      increaseBytesRead(red.toULong())

    return red
  }

  override fun close() {
    stream.close()
  }

  private fun increaseBytesRead(by: ULong) {
    bytesRead += by
    if (bytesRead > limit)
      throw errorProvider()
  }
}