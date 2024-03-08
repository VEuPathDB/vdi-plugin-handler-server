package vdi.server.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.veupathdb.vdi.lib.common.field.DatasetID
import org.veupathdb.vdi.lib.common.model.VDIDatasetMeta
import vdi.consts.FieldName

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class ImportDetails(
  @JsonProperty(FieldName.VDIID)
  val vdiID: DatasetID,

  @JsonProperty(FieldName.Meta)
  val meta: VDIDatasetMeta
)
