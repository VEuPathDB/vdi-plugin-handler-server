package vdi.conf

import vdi.util.SecretString

private const val DB_NAME_PREFIX = "DB_CONNECTION_NAME_"
private const val DB_LDAP_PREFIX = "DB_CONNECTION_LDAP_"
private const val DB_USER_PREFIX = "DB_CONNECTION_USER_"
private const val DB_PASS_PREFIX = "DB_CONNECTION_PASS_"

private const val DB_ENV_VAR_INIT_CAPACITY = 12

/**
 * Database Configuration Map
 *
 * Mapping of [DatabaseConfiguration] instances keyed on the name for each that
 * was provided on the environment.
 *
 * @author Elizabeth Paige Harper - https://github.com/foxcapades
 * @since 1.0.0
 *
 * @constructor
 *
 * Creating a new [DatabaseConfigurationMap] instance is done by parsing an
 * environment map of [String] key/value pairs looking for keys starting with
 * specific prefixes.
 *
 * **Prefixes**
 * ```
 * DB_CONNECTION_NAME_  - This prefix is used for variables that define a
 *                        database configuration variable group's name value.
 *
 * DB_CONNECTION_LDAP_  - This prefix is used for variables that define a
 *                        database configuration variable group's LDAP query
 *                        string.
 *
 * DB_CONNECTION_USER_  - This prefix is used for variables that define a
 *                        database configuration variable group's auth username
 *                        value.
 *
 * DB_CONNECTION_PASS_  - This prefix is used for variables that define a
 *                        database configuration variable group's auth password
 *                        value.
 * ```
 *
 * Once a key with one of the specified prefixes is found, the key will be split
 * on the prefix length to get a suffix value.  This suffix value is then used
 * to test for the existence of the other 3 expected keys by using the prefixes
 * that were not encountered.
 *
 * For example, if we were to encounter the key `DB_CONNECTION_NAME_FOO` in the
 * environment map, we would know to test for `DB_CONNECTION_LDAP_FOO`,
 * `DB_CONNECTION_USER_FOO`, and `DB_CONNECTION_PASS_FOO` in the map as well to
 * complete the config variable group.
 *
 * If one part of a database config variable group is present, then all 4 must
 * exist with the same suffix.  This means that it is an error to pass in an
 * environment that just contains `DB_CONNECTION_LDAP_FOO` but contains no other
 * variables with the other defined prefixes and the suffix "FOO".
 *
 * @param environment Map of environment variable names to values.
 */
class DatabaseConfigurationMap(environment: Map<String, String> = System.getenv())
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


private fun parseDatabaseConfigs(environment: Map<String, String>) =
  environment.parse()

private fun Map<String, String>.parse(): Map<String, DatabaseConfiguration> {
  val out  = HashMap<String, DatabaseConfiguration>(DB_ENV_VAR_INIT_CAPACITY)
  val seen = HashSet<String>(12)

  forEach { (key, _) ->
    if (key in seen)
      return@forEach

    when {
      key.startsWith(DB_NAME_PREFIX) -> parse(key.substring(DB_NAME_PREFIX.length), seen, out)
      key.startsWith(DB_LDAP_PREFIX) -> parse(key.substring(DB_LDAP_PREFIX.length), seen, out)
      key.startsWith(DB_USER_PREFIX) -> parse(key.substring(DB_USER_PREFIX.length), seen, out)
      key.startsWith(DB_PASS_PREFIX) -> parse(key.substring(DB_PASS_PREFIX.length), seen, out)
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

  seen.add(DB_NAME_PREFIX + suffix)
  seen.add(DB_LDAP_PREFIX + suffix)
  seen.add(DB_USER_PREFIX + suffix)
  seen.add(DB_PASS_PREFIX + suffix)

  out[db.name] = db
}

private fun Map<String, String>.parse(suffix: String) =
  DatabaseConfiguration(
    name = get(DB_NAME_PREFIX + suffix) ?: parsingFailed(suffix),
    ldap = get(DB_LDAP_PREFIX + suffix)?.let(::SecretString) ?: parsingFailed(suffix),
    user = get(DB_USER_PREFIX + suffix)?.let(::SecretString) ?: parsingFailed(suffix),
    pass = get(DB_PASS_PREFIX + suffix)?.let(::SecretString) ?: parsingFailed(suffix),
  )

private fun parsingFailed(suffix: String): Nothing {
  throw RuntimeException(
    "One or more database connection configuration environment variables was not set with the suffix: $suffix"
  )
}