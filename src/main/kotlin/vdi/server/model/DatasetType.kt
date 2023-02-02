package vdi.server.model

import com.fasterxml.jackson.annotation.JsonProperty
import vdi.Const.FieldName

data class DatasetType(
  @JsonProperty(FieldName.Name)
  val name: String,

  @JsonProperty(FieldName.Version)
  val version: String,
)