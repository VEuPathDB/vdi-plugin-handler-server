package vdi.util

import com.fasterxml.jackson.core.JacksonException
import io.ktor.http.content.*
import io.ktor.util.*
import io.ktor.utils.io.jvm.javaio.*
import org.slf4j.LoggerFactory
import org.veupathdb.vdi.lib.json.JSON
import vdi.components.io.BoundedInputStream
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.reflect.KClass

private const val MaxSizeToLoadInMemory = 16384uL

private val logger = LoggerFactory.getLogger("multipart-util")

fun <T : Any> PartData.parseAsJson(maxInputSize: ULong, type: KClass<T>): T =
  with(
    when (this) {
      is PartData.BinaryChannelItem -> BoundedInputStream(provider().toInputStream(), maxInputSize)
      is PartData.BinaryItem        -> BoundedInputStream(provider().asStream(), maxInputSize)
      is PartData.FileItem          -> BoundedInputStream(streamProvider(), maxInputSize)
      is PartData.FormItem          -> BoundedInputStream(value.byteInputStream(), maxInputSize)
    }
  ) {
    if (maxInputSize > MaxSizeToLoadInMemory)
      parseStreamAsJson(this, type)
    else
      parseStringAsJson(this, type)
  }

private fun <T : Any> parseStringAsJson(stream: InputStream, type: KClass<T>): T =
  stream.use {
    val body = InputStreamReader(it).readText()

    try {
      JSON.readValue(body, type.java)
    } catch (e: JacksonException) {
      logger.error("jackson encountered an error while attempting to parse the following value: {}", body)
      throw e
    }
  }

private fun <T : Any> parseStreamAsJson(stream: InputStream, type: KClass<T>): T =
  stream.use {
    try {
      JSON.readValue(stream, type.java)
    } catch (e: JacksonException) {
      logger.error("jackson encountered an error while attempting to parse a JSON stream of an unknown size")
      throw e
    }
  }