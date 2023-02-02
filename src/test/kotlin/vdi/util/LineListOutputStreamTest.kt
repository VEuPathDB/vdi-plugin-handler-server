package vdi.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

@DisplayName("LineListOutputStream")
class LineListOutputStreamTest {

  @Suppress("UNCHECKED_CAST")
  val mockCollection = mock(MutableCollection::class.java) as MutableCollection<Any>

  @Test
  @DisplayName("Appends buffer on close.")
  fun t1() {
    val input = "Hello world!".byteInputStream()

    LineListOutputStream(mockCollection).use { input.transferTo(it) }

    verify(mockCollection, times(1)).add("Hello world!")
    verifyNoMoreInteractions(mockCollection)
  }

  @Test
  @DisplayName("Appends text lines as individual items.")
  fun t2() {
    val input = "Goodbye\ncruel\nworld!".byteInputStream()

    LineListOutputStream(mockCollection).use { input.transferTo(it) }

    verify(mockCollection, times(1)).add("Goodbye")
    verify(mockCollection, times(1)).add("cruel")
    verify(mockCollection, times(1)).add("world!")
    verifyNoMoreInteractions(mockCollection)
  }

  @Test
  @DisplayName("Does not append trailing empty line.")
  fun t3() {
    val input = "Waka waka waka!\n".byteInputStream()

    LineListOutputStream(mockCollection).use { input.transferTo(it) }

    verify(mockCollection, times(1)).add("Waka waka waka!")
    verifyNoMoreInteractions(mockCollection)
  }
}