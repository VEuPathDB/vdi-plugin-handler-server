package vdi.server.model

import com.fasterxml.jackson.annotation.JsonProperty
import vdi.Const

data class SimpleErrorResponse(
  @JsonProperty(Const.FieldName.Message)
  val message: String
)
