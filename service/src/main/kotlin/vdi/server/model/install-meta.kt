package vdi.server.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import vdi.consts.FieldName

@JsonIgnoreProperties(ignoreUnknown = true)
data class InstallMetaRequest(
  @JsonProperty(FieldName.VDIID)
  val vdiID: String,

  @JsonProperty(FieldName.ProjectID)
  val projectID: String,

  @JsonProperty(FieldName.Meta)
  val meta: DatasetMeta,
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class DatasetMeta(
  @JsonProperty(FieldName.Type)
  val type: DatasetMetaType,

  @JsonProperty(FieldName.Projects)
  val projects: Collection<String>,

  @JsonProperty(FieldName.Owner)
  val owner: String,

  @JsonProperty(FieldName.Name)
  val name: String,

  @JsonProperty(FieldName.Summary)
  val summary: String,

  @JsonProperty(FieldName.Description)
  val description: String,

  @JsonProperty(FieldName.Dependencies)
  val dependencies: Collection<DatasetMetaDependency>
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class DatasetMetaType(
  @JsonProperty(FieldName.Name)
  val name: String,

  @JsonProperty(FieldName.Version)
  val version: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class DatasetMetaDependency(

  @JsonProperty(FieldName.ResourceIdentifier)
  val resourceIdentifier: String,

  @JsonProperty(FieldName.ResourceVersion)
  val resourceVersion: String,

  @JsonProperty(FieldName.ResourceDisplayName)
  val resourceDisplayName: String,
)