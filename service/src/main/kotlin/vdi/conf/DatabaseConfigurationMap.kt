package vdi.conf

import org.veupathdb.vdi.lib.common.env.*
import org.veupathdb.vdi.lib.common.field.DataType
import org.veupathdb.vdi.lib.common.field.ProjectID
import org.veupathdb.vdi.lib.common.field.SecretString
import vdi.model.DBPlatform

private const val DB_ENV_VAR_INIT_CAPACITY = 12

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
class DatabaseConfigurationMap(environment: Environment) : Map<DBKey, DatabaseConfiguration>
{
  private val raw: Map<DBKey, DatabaseConfiguration> = parseDatabaseConfigs(environment)

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
}

private fun parseDatabaseConfigs(environment: Environment) =
  environment.parse()

private fun Environment.parse(): Map<DBKey, DatabaseConfiguration> {
  val out  = HashMap<DBKey, DatabaseConfiguration>(DB_ENV_VAR_INIT_CAPACITY)

  val seen = HashSet<String>(12)

  // Iterate through all the environment variables available to the server
  // process looking for variables that start with one of the known prefixes.
  //
  // When a matching variable is found it will be passed to the `parse` method
  // which will attempt to build a full `DatabaseConfiguration` instance by
  // requiring the remaining variables for that config set exist in the
  // environment.
  //
  // `parse` will also update the `seen` set with the config set key to avoid
  // processing the same config set multiple times.
  forEach { (key, _) ->
    when {
      key.startsWith(EnvKey.AppDB.DBEnabledPrefix)        -> parse(key.substring(EnvKey.AppDB.DBEnabledPrefix.length), seen, out)
      key.startsWith(EnvKey.AppDB.DBConnectionNamePrefix) -> parse(key.substring(EnvKey.AppDB.DBConnectionNamePrefix.length), seen, out)
      key.startsWith(EnvKey.AppDB.DBLDAPPrefix)           -> parse(key.substring(EnvKey.AppDB.DBLDAPPrefix.length), seen, out)
      key.startsWith(EnvKey.AppDB.DBPassPrefix)           -> parse(key.substring(EnvKey.AppDB.DBPassPrefix.length), seen, out)
      key.startsWith(EnvKey.AppDB.DBDataSchemaPrefix)     -> parse(key.substring(EnvKey.AppDB.DBDataSchemaPrefix.length), seen, out)
      key.startsWith(EnvKey.AppDB.DBNamePrefix)           -> parse(key.substring(EnvKey.AppDB.DBNamePrefix.length), seen, out)
      key.startsWith(EnvKey.AppDB.DBHostPrefix)           -> parse(key.substring(EnvKey.AppDB.DBHostPrefix.length), seen, out)
      key.startsWith(EnvKey.AppDB.DBPortPrefix)           -> parse(key.substring(EnvKey.AppDB.DBPortPrefix.length), seen, out)
      key.startsWith(EnvKey.AppDB.DBPlatformPrefix)       -> parse(key.substring(EnvKey.AppDB.DBPlatformPrefix.length), seen, out)
      key.startsWith(EnvKey.AppDB.DBConnectionDataTypes)  -> parse(key.substring(EnvKey.AppDB.DBConnectionDataTypes.length), seen, out)
    }
  }

  if (out.isEmpty())
    throw IllegalStateException("No enabled application databases are configured.")

  return out
}


private fun Environment.parse(key: String, names: MutableSet<String>, out: MutableMap<DBKey, DatabaseConfiguration>) {
  // If a database config set with the given key has already been seen (present
  // in the names set) then skip it because we've already processed this config
  // set.
  if (key in names)
    return

  // Add the key to the seen config set names set.
  names.add(key)

  // Build the DatabaseConfiguration instance from the environment.
  //
  // If the enabled flag for the database config set is `false`, then skip the
  // config set.
  if (reqBool(EnvKey.AppDB.DBEnabledPrefix + key)) {
    val name  = require(EnvKey.AppDB.DBConnectionNamePrefix + key)
    val ldap  = optional(EnvKey.AppDB.DBLDAPPrefix + key)
    val types = optList(EnvKey.AppDB.DBConnectionDataTypes)?.map(DataType::of) ?: listOf(Wildcard)

    val tmp = if (ldap == null) {
      DatabaseConfiguration(
        connectionName = name,
        // We use the DB schema as the username.
        user       = require(EnvKey.AppDB.DBDataSchemaPrefix + key),
        pass       = SecretString(require(EnvKey.AppDB.DBPassPrefix + key)),
        dataSchema = require(EnvKey.AppDB.DBDataSchemaPrefix + key),
        platform   = optional(EnvKey.AppDB.DBPlatformPrefix + key)?.let(DBPlatform::fromPlatformString) ?: DBPlatform.Oracle,
        port       = reqUShort(EnvKey.AppDB.DBPortPrefix + key),
        host       = require(EnvKey.AppDB.DBHostPrefix + key),
        dbName     = require(EnvKey.AppDB.DBNamePrefix + key),
        ldap       = null,
      )
    } else {
      if (optional(EnvKey.AppDB.DBPortPrefix + key) != null || optional(EnvKey.AppDB.DBHostPrefix + key) != null || optional(EnvKey.AppDB.DBNamePrefix + key) != null)
        throw IllegalStateException("environment specifies both LDAP and direct database configuration options for env group $key")

      DatabaseConfiguration(
        connectionName = name,
        // We use the DB schema as the username.
        ldap       = ldap,
        user       = require(EnvKey.AppDB.DBDataSchemaPrefix + key),
        pass       = SecretString(require(EnvKey.AppDB.DBPassPrefix + key)),
        dataSchema = require(EnvKey.AppDB.DBDataSchemaPrefix + key),
        platform   = optional(EnvKey.AppDB.DBPlatformPrefix + key)?.let(DBPlatform::fromPlatformString) ?: DBPlatform.Oracle,
        port       = null,
        host       = null,
        dbName     = null,
      )
    }

    for (dt in types)
      out[name to dt] = tmp
  }
}
