package vdi.server.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import vdi.consts.Const.FieldName

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class ImportDetails(
  @JsonProperty(FieldName.VDIID)
  val vdiID: String,

  @JsonProperty(FieldName.Type)
  val type: DatasetType,

  @JsonProperty(FieldName.Projects)
  val projects: Collection<String>,

  @JsonProperty(FieldName.Owner)
  val owner: String,

  @JsonProperty(FieldName.Name)
  val name: String,

  @JsonProperty(FieldName.Summary)
  val summary: String?,

  @JsonProperty(FieldName.Description)
  val description: String?,

  @JsonProperty(FieldName.Dependencies)
  val dependencies: Collection<DatasetDependency>
)
