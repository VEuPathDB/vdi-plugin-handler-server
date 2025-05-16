package vdi.conf

import org.veupathdb.vdi.lib.config.LDAPConfig
import org.veupathdb.vdi.lib.config.PluginConfig
import org.veupathdb.vdi.lib.config.PluginScriptConfigs

data class RawServiceConfiguration(
  val ldap: LDAPConfig,
  val siteBuild: String,
  val http: HTTPConfig,
  override val scripts: PluginScriptConfigs?,
  override val customPath: String?,
  override val installRoot: String?,
  val installTargets: Set<InstallTargetConfig>,
): PluginConfig

