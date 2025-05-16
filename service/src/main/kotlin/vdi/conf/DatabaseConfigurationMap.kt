package vdi.conf

import org.slf4j.LoggerFactory
import org.veupathdb.vdi.lib.common.field.DataType
import org.veupathdb.vdi.lib.common.field.ProjectID
import org.veupathdb.vdi.lib.config.DirectDatabaseConnectionConfig
import org.veupathdb.vdi.lib.config.LDAPDatabaseConnectionConfig
import vdi.model.DBPlatform

private val Wildcard = DataType.of("*")

typealias DBKey = Pair<ProjectID, DataType>

/**
 * Database Configuration Map
 *
 * Mapping of [DatabaseConfiguration] instances keyed on the name for each that
 * was provided on the environment.
 *
 * @author Elizabeth Paige Harper - https://github.com/foxcapades
 * @since 1.0.0
 */
class DatabaseConfigurationMap(raw: RawServiceConfiguration) : Map<DBKey, DatabaseConfiguration> {
  private val log = LoggerFactory.getLogger(javaClass)

  private val raw: Map<DBKey, DatabaseConfiguration> = parseDatabaseConfigs(raw)

  override val entries: Set<Map.Entry<DBKey, DatabaseConfiguration>>
    get() = raw.entries

  override val keys: Set<DBKey>
    get() = raw.keys

  override val size: Int
    get() = raw.size

  override val values: Collection<DatabaseConfiguration>
    get() = raw.values

  override fun isEmpty() = raw.isEmpty()

  override fun get(key: DBKey): DatabaseConfiguration? =
    raw[key] ?: if (key.second != Wildcard) raw[key.first to Wildcard] else null

  override fun containsValue(value: DatabaseConfiguration) = raw.containsValue(value)

  override fun containsKey(key: DBKey): Boolean =
    raw.containsKey(key) || (key.second != Wildcard && raw.containsKey(key.first to Wildcard))

  private fun parseDatabaseConfigs(raw: RawServiceConfiguration): Map<DBKey, DatabaseConfiguration> =
    raw.installTargets.asSequence()
      .filter { it.enabled.also { e -> if (!e) log.warn("install target ${it.targetName} is disabled") } }
      .onEach { log.info("install target ${it.targetName} is enabled") }
      .flatMap { conf ->
        val tgt = initTarget(conf)
        conf.dataTypes.asSequence()
          .map { (conf.targetName to DataType.of(it)) to tgt }
      }
      .toMap()

  private fun initTarget(conf: InstallTargetConfig): DatabaseConfiguration {
    return when (val db = conf.dataDB) {
      is DirectDatabaseConnectionConfig -> {
        val platform = DBPlatform.fromPlatformString(db.platform)
        DirectDatabaseConfiguration(
          name     = conf.targetName,
          user     = db.username,
          pass     = db.password,
          platform = platform,
          server   = db.server.toHostAddress(platform.defaultPort),
          dbName   = db.dbName,
        )
      }
      is LDAPDatabaseConnectionConfig -> {
        LDAPDatabaseConfiguration(
          name     = conf.targetName,
          lookupCn = db.lookupCN,
          user     = db.username,
          pass     = db.password,
        )
      }
    }
  }
}
