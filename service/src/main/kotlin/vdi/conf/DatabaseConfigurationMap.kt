package vdi.conf

import org.veupathdb.vdi.lib.common.env.*
import org.veupathdb.vdi.lib.common.field.SecretString
import vdi.model.DBPlatform

private const val DB_ENABLED_PREFIX   = EnvKey.AppDB.DBEnabledPrefix
private const val DB_CONN_NAME_PREFIX = EnvKey.AppDB.DBConnectionNamePrefix
private const val DB_LDAP_PREFIX      = EnvKey.AppDB.DBLDAPPrefix
private const val DB_PASS_PREFIX      = EnvKey.AppDB.DBPassPrefix
private const val DB_SCHEMA_PREFIX    = EnvKey.AppDB.DBDataSchemaPrefix
private const val DB_NAME_PREFIX      = EnvKey.AppDB.DBNamePrefix
private const val DB_HOST_PREFIX      = EnvKey.AppDB.DBHostPrefix
private const val DB_PORT_PREFIX      = EnvKey.AppDB.DBPortPrefix
private const val DB_PLATFORM_PREFIX  = EnvKey.AppDB.DBPlatformPrefix

private const val DB_ENV_VAR_INIT_CAPACITY = 12

/**
 * Database Configuration Map
 *
 * Mapping of [DatabaseConfiguration] instances keyed on the name for each that
 * was provided on the environment.
 *
 * @author Elizabeth Paige Harper - https://github.com/foxcapades
 * @since 1.0.0
 */
class DatabaseConfigurationMap(environment: Environment)
: Map<String, DatabaseConfiguration>
{
  private val raw: Map<String, DatabaseConfiguration> = parseDatabaseConfigs(environment)

  override val entries: Set<Map.Entry<String, DatabaseConfiguration>>
    get() = raw.entries

  override val keys: Set<String>
    get() = raw.keys

  override val size: Int
    get() = raw.size

  override val values: Collection<DatabaseConfiguration>
    get() = raw.values

  override fun isEmpty() = raw.isEmpty()

  override fun get(key: String) = raw[key]

  override fun containsValue(value: DatabaseConfiguration) = raw.containsValue(value)

  override fun containsKey(key: String) = raw.containsKey(key)
}

private fun parseDatabaseConfigs(environment: Environment) =
  environment.parse()

private fun Environment.parse(): Map<String, DatabaseConfiguration> {
  val out  = HashMap<String, DatabaseConfiguration>(DB_ENV_VAR_INIT_CAPACITY)

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
      key.startsWith(DB_ENABLED_PREFIX)   -> parse(key.substring(DB_ENABLED_PREFIX.length), seen, out)
      key.startsWith(DB_CONN_NAME_PREFIX) -> parse(key.substring(DB_CONN_NAME_PREFIX.length), seen, out)
      key.startsWith(DB_LDAP_PREFIX)      -> parse(key.substring(DB_LDAP_PREFIX.length), seen, out)
      key.startsWith(DB_PASS_PREFIX)      -> parse(key.substring(DB_PASS_PREFIX.length), seen, out)
      key.startsWith(DB_SCHEMA_PREFIX)    -> parse(key.substring(DB_SCHEMA_PREFIX.length), seen, out)
      key.startsWith(DB_NAME_PREFIX)      -> parse(key.substring(DB_NAME_PREFIX.length), seen, out)
      key.startsWith(DB_HOST_PREFIX)      -> parse(key.substring(DB_HOST_PREFIX.length), seen, out)
      key.startsWith(DB_PORT_PREFIX)      -> parse(key.substring(DB_PORT_PREFIX.length), seen, out)
      key.startsWith(DB_PLATFORM_PREFIX)  -> parse(key.substring(DB_PLATFORM_PREFIX.length), seen, out)
    }
  }

  if (out.isEmpty())
    throw IllegalStateException("No enabled application databases are configured.")

  return out
}


private fun Environment.parse(key: String, names: MutableSet<String>, out: MutableMap<String, DatabaseConfiguration>) {
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
  if (reqBool(DB_ENABLED_PREFIX + key)) {
    val name = require(DB_CONN_NAME_PREFIX + key)
    val ldap = optional(DB_LDAP_PREFIX + key)

    if (ldap == null) {
      out[name] = DatabaseConfiguration(
        connectionName = name,
        // We use the DB schema as the username.
        user       = require(DB_SCHEMA_PREFIX + key),
        pass       = SecretString(require(DB_PASS_PREFIX + key)),
        dataSchema = require(DB_SCHEMA_PREFIX + key),
        platform   = optional(DB_PLATFORM_PREFIX + key)?.let(DBPlatform::fromPlatformString) ?: DBPlatform.Oracle,
        port       = reqUShort(DB_PORT_PREFIX + key),
        host       = require(DB_HOST_PREFIX + key),
        dbName     = require(DB_NAME_PREFIX + key),
        ldap       = null,
      )
    } else {
      if (optional(DB_PORT_PREFIX + key) != null || optional(DB_HOST_PREFIX + key) != null || optional(DB_NAME_PREFIX + key) != null)
        throw IllegalStateException("environment specifies both LDAP and direct database configuration options for env group $key")

      out[name] = DatabaseConfiguration(
        connectionName = name,
        // We use the DB schema as the username.
        ldap       = ldap,
        user       = require(DB_SCHEMA_PREFIX + key),
        pass       = SecretString(require(DB_PASS_PREFIX + key)),
        dataSchema = require(DB_SCHEMA_PREFIX + key),
        platform   = optional(DB_PLATFORM_PREFIX + key)?.let(DBPlatform::fromPlatformString) ?: DBPlatform.Oracle,
        port       = null,
        host       = null,
        dbName     = null,
      )
    }
  }
}
