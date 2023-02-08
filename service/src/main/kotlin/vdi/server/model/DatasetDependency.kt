package vdi.server.model

import com.fasterxml.jackson.annotation.JsonProperty
import vdi.consts.FieldName

data class DatasetDependency(
  @JsonProperty(FieldName.ResourceIdentifier)
  val resourceIdentifier: String,

  @JsonProperty(FieldName.ResourceVersion)
  val resourceVersion: String,

  @JsonProperty(FieldName.ResourceDisplayName)
  val resourceDisplayName: String,
)