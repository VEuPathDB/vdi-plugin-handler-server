package vdi.server.model

import com.fasterxml.jackson.annotation.JsonProperty
import vdi.consts.FieldName

data class WarningsListResponse(
  @JsonProperty(FieldName.Messages)
  val messages: Collection<String>,
)
