package vdi.server.model

import com.fasterxml.jackson.annotation.JsonProperty
import vdi.consts.Const

data class DatasetDependency(
  @JsonProperty(Const.FieldName.ResourceIdentifier)
  val resourceIdentifier: String,

  @JsonProperty(Const.FieldName.ResourceVersion)
  val resourceVersion: String,

  @JsonProperty(Const.FieldName.ResourceDisplayName)
  val resourceDisplayName: String,
)