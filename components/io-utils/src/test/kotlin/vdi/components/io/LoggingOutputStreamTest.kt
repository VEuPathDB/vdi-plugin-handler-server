package vdi.components.io

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.slf4j.Logger

@DisplayName("LoggingOutputStream")
class LoggingOutputStreamTest {

  val prefix     = "PREFIX"
  val mockLogger = mock(Logger::class.java)

  @Test
  @DisplayName("Writes buffer on close.")
  fun t1() {
    val inputStream = "Hello world!".byteInputStream()

    LoggingOutputStream(prefix, mockLogger).use { inputStream.transferTo(it) }

    verify(mockLogger, times(1)).info("{} {}", prefix, "Hello world!")
    verifyNoMoreInteractions(mockLogger)
  }

  @Test
  @DisplayName("Writes text lines as log lines.")
  fun t2() {
    val inputStream = "Goodbye\ncruel\nworld!".byteInputStream()

    LoggingOutputStream(prefix, mockLogger).use { inputStream.transferTo(it) }

    verify(mockLogger, times(1)).info("{} {}", prefix, "Goodbye")
    verify(mockLogger, times(1)).info("{} {}", prefix, "cruel")
    verify(mockLogger, times(1)).info("{} {}", prefix, "world!")
    verifyNoMoreInteractions(mockLogger)
  }

  @Test
  @DisplayName("Doesn't write out trailing empty line.")
  fun t3() {
    val inputStream = "Waka waka waka!\n".byteInputStream()

    LoggingOutputStream(prefix, mockLogger).use { inputStream.transferTo(it) }

    verify(mockLogger, times(1)).info("{} {}", prefix, "Waka waka waka!")
    verifyNoMoreInteractions(mockLogger)
  }
}