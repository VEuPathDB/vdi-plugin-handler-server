package vdi.components.http.errors

import com.fasterxml.jackson.annotation.JsonProperty


data class SimpleErrorResponse(
  @JsonProperty("message")
  val message: String
)
