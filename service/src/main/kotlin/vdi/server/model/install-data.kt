package vdi.server.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.veupathdb.vdi.lib.common.field.DatasetID
import org.veupathdb.vdi.lib.common.field.ProjectID

@JsonIgnoreProperties(ignoreUnknown = true)
data class InstallDetails(
  @JsonProperty("vdiID")
  val vdiID: DatasetID,

  @JsonProperty("projectID")
  val projectID: ProjectID,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class InstallDataSuccessResponse(

  @JsonProperty("warnings")
  val warnings: Collection<String>
)