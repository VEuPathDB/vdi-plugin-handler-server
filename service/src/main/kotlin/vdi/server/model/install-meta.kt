package vdi.server.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.veupathdb.vdi.lib.common.field.DatasetID
import org.veupathdb.vdi.lib.common.field.ProjectID
import org.veupathdb.vdi.lib.common.model.VDIDatasetMeta
import vdi.consts.FieldName

@JsonIgnoreProperties(ignoreUnknown = true)
data class InstallMetaRequest(
  @JsonProperty(FieldName.VDIID)
  val vdiID: DatasetID,

  @JsonProperty(FieldName.ProjectID)
  val projectID: ProjectID,

  @JsonProperty(FieldName.Meta)
  val meta: VDIDatasetMeta,
)
