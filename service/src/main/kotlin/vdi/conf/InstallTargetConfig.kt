package vdi.conf

import com.fasterxml.jackson.annotation.JsonProperty
import org.veupathdb.vdi.lib.config.DatabaseConnectionConfig

data class InstallTargetConfig(
  val targetName: String,

  @param:JsonProperty("dataDb")
  @field:JsonProperty("dataDb")
  val dataDB: DatabaseConnectionConfig,

  val enabled: Boolean = true,
  val dataTypes: Set<String> = setOf("*"),
)
