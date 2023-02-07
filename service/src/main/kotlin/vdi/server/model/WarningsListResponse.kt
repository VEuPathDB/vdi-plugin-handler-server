package vdi.server.model

import com.fasterxml.jackson.annotation.JsonProperty
import vdi.consts.Const

data class WarningsListResponse(
  @JsonProperty(Const.FieldName.Messages)
  val messages: Collection<String>,
)
