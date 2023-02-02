package vdi.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.slf4j.Logger

@DisplayName("LoggingOutputStream")
class LoggingOutputStreamTest {

  val mockLogger = mock(Logger::class.java)

  @Test
  @DisplayName("Writes buffer on close.")
  fun t1() {
    val inputStream = "Hello world!".byteInputStream()

    LoggingOutputStream(mockLogger).use { inputStream.transferTo(it) }

    verify(mockLogger, times(1)).info("Hello world!")
    verifyNoMoreInteractions(mockLogger)
  }

  @Test
  @DisplayName("Writes text lines as log lines.")
  fun t2() {
    val inputStream = "Goodbye\ncruel\nworld!".byteInputStream()

    LoggingOutputStream(mockLogger).use { inputStream.transferTo(it) }

    verify(mockLogger, times(1)).info("Goodbye")
    verify(mockLogger, times(1)).info("cruel")
    verify(mockLogger, times(1)).info("world!")
    verifyNoMoreInteractions(mockLogger)
  }

  @Test
  @DisplayName("Doesn't write out trailing empty line.")
  fun t3() {
    val inputStream = "Waka waka waka!\n".byteInputStream()

    LoggingOutputStream(mockLogger).use { inputStream.transferTo(it) }

    verify(mockLogger, times(1)).info("Waka waka waka!")
    verifyNoMoreInteractions(mockLogger)
  }
}