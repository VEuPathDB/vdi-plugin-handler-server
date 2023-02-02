package vdi.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import java.io.OutputStream

@DisplayName("BoundedInputStream")
class BoundedInputStreamTest {

  object TrashOutputStream : OutputStream() { override fun write(b: Int) { } }

  @Test
  @DisplayName("throws when the byte limit is exceeded")
  fun t1() {
    val input  = "Hello world!".byteInputStream()
    val stream = BoundedInputStream(input, 5u)

    assertThrows<IOException> {
      stream.transferTo(TrashOutputStream)
    }
  }

  @Test
  @DisplayName("does not throw when the byte limit is not exceeded")
  fun t2() {
    val input  = "Hello world!".byteInputStream()
    val stream = BoundedInputStream(input, 100u)

    assertDoesNotThrow {
      stream.transferTo(TrashOutputStream)
    }
  }
}