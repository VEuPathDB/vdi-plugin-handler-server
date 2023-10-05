package vdi.conf

import org.veupathdb.vdi.lib.common.env.EnvKey
import org.veupathdb.vdi.lib.common.env.Environment
import org.veupathdb.vdi.lib.common.env.reqBool
import org.veupathdb.vdi.lib.common.env.require
import org.veupathdb.vdi.lib.common.field.SecretString

private const val DB_ENABLED_PREFIX = EnvKey.AppDB.DBEnabledPrefix
private const val DB_NAME_PREFIX    = EnvKey.AppDB.DBNamePrefix
private const val DB_LDAP_PREFIX    = EnvKey.AppDB.DBLDAPPrefix
private const val DB_USER_PREFIX    = EnvKey.AppDB.DBUserPrefix
private const val DB_PASS_PREFIX    = EnvKey.AppDB.DBPassPrefix
private const val DB_SCHEMA_PREFIX  = EnvKey.AppDB.DBDataSchemaPrefix

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
  private val raw: Map<String, DatabaseConfiguration>

  init {
    raw = parseDatabaseConfigs(environment)
  }

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

  forEach { (key, _) ->
    when {
      key.startsWith(DB_ENABLED_PREFIX) -> parse(key.substring(DB_ENABLED_PREFIX.length), seen, out)
      key.startsWith(DB_SCHEMA_PREFIX)  -> parse(key.substring(DB_SCHEMA_PREFIX.length), seen, out)
      key.startsWith(DB_NAME_PREFIX)    -> parse(key.substring(DB_NAME_PREFIX.length), seen, out)
      key.startsWith(DB_LDAP_PREFIX)    -> parse(key.substring(DB_LDAP_PREFIX.length), seen, out)
      key.startsWith(DB_USER_PREFIX)    -> parse(key.substring(DB_USER_PREFIX.length), seen, out)
      key.startsWith(DB_PASS_PREFIX)    -> parse(key.substring(DB_PASS_PREFIX.length), seen, out)
    }
  }

  return out
}

private fun Environment.parse(key: String, names: MutableSet<String>, out: MutableMap<String, DatabaseConfiguration>) {
  if (key in names)
    return

  if (reqBool(DB_ENABLED_PREFIX + key)) {
    val name = require(DB_NAME_PREFIX + key)

    out[name] = DatabaseConfiguration(
      name       = name,
      ldap       = require(DB_LDAP_PREFIX + key),
      user       = require(DB_USER_PREFIX + key),
      pass       = SecretString(require(DB_PASS_PREFIX + key)),
      dataSchema = require(DB_SCHEMA_PREFIX + key),
    )
  }
}
