package vdi.consts

object ProcessOpt {

  /**
   * Maximum number of bytes for the "details" field in the import processing
   * multipart post body.
   */
  const val MaxDetailsFieldBytes = 16384uL

  /**
   * What we name the payload posted to the handler server with the import
   * processing multipart post body.
   */
  const val ImportPayloadName = "import-payload.tar.gz"
}