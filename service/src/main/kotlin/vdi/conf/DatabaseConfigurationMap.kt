package vdi.conf

import org.veupathdb.vdi.lib.common.env.EnvKey
import vdi.components.common.EnvironmentAccessor
import vdi.components.common.SecretString

private const val DB_NAME_PREFIX   = EnvKey.AppDB.DBNamePrefix
private const val DB_LDAP_PREFIX   = EnvKey.AppDB.DBLDAPPrefix
private const val DB_USER_PREFIX   = EnvKey.AppDB.DBUserPrefix
private const val DB_PASS_PREFIX   = EnvKey.AppDB.DBPassPrefix
private const val DB_SCHEMA_PREFIX = EnvKey.AppDB.DBSchemaPrefix

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
class DatabaseConfigurationMap(environment: EnvironmentAccessor)
: Map<String, DatabaseConfiguration>
{
  private val raw: Map<String, DatabaseConfiguration>

  init {
    raw = parseDatabaseConfigs(environment.rawEnvironment())
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


private fun parseDatabaseConfigs(environment: Map<String, String>) =
  environment.parse()

private fun Map<String, String>.parse(): Map<String, DatabaseConfiguration> {
  val out  = HashMap<String, DatabaseConfiguration>(DB_ENV_VAR_INIT_CAPACITY)
  val seen = HashSet<String>(12)

  forEach { (key, _) ->
    if (key in seen)
      return@forEach

    when {
      key.startsWith(DB_SCHEMA_PREFIX) -> parse(key.substring(DB_SCHEMA_PREFIX.length), seen, out)
      key.startsWith(DB_NAME_PREFIX)   -> parse(key.substring(DB_NAME_PREFIX.length), seen, out)
      key.startsWith(DB_LDAP_PREFIX)   -> parse(key.substring(DB_LDAP_PREFIX.length), seen, out)
      key.startsWith(DB_USER_PREFIX)   -> parse(key.substring(DB_USER_PREFIX.length), seen, out)
      key.startsWith(DB_PASS_PREFIX)   -> parse(key.substring(DB_PASS_PREFIX.length), seen, out)
    }
  }


  return out
}

private fun Map<String, String>.parse(
  suffix: String,
  seen:   MutableSet<String>,
  out:    MutableMap<String, DatabaseConfiguration>
) {
  val db = parse(suffix)

  seen.add(DB_SCHEMA_PREFIX + suffix)
  seen.add(DB_NAME_PREFIX + suffix)
  seen.add(DB_LDAP_PREFIX + suffix)
  seen.add(DB_USER_PREFIX + suffix)
  seen.add(DB_PASS_PREFIX + suffix)

  out[db.name] = db
}

private fun Map<String, String>.parse(suffix: String) =
  DatabaseConfiguration(
    name   = get(DB_NAME_PREFIX + suffix) ?: parsingFailed(suffix),
    ldap   = get(DB_LDAP_PREFIX + suffix) ?: parsingFailed(suffix),
    user   = get(DB_USER_PREFIX + suffix) ?: parsingFailed(suffix),
    pass   = get(DB_PASS_PREFIX + suffix)?.let(::SecretString) ?: parsingFailed(suffix),
    schema = get(DB_SCHEMA_PREFIX + suffix) ?: parsingFailed(suffix)
  )

private fun parsingFailed(suffix: String): Nothing {
  throw RuntimeException(
    "One or more database connection configuration environment variables was not set with the suffix: $suffix"
  )
}