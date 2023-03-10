package vdi.components.common

interface EnvironmentAccessor {
  /**
   * Requires the existence of a non-blank environment variable with a name
   * matching the given [key] value.
   *
   * @param key Name of the environment variable to require.
   *
   * @return The required, non-null, non-blank environment variable value.
   */
  fun require(key: String): String

  /**
   * Looks up a target environment variable with a name matching the given [key]
   * value, and returns it if it exists and is not `null`.
   *
   * If the target environment variable does exist on the environment, but is
   * blank, `null` will be returned.
   *
   * @param key Name of the environment variable to require.
   *
   * @return The target environment variable, if it is set and non-blank,
   * otherwise `null`.
   */
  fun optional(key: String): String?

  /**
   * Requires the existence of a non-blank environment variable with a name
   * matching the given [key], mapped to type [T] by the given mapping function
   * ([fn]).
   *
   * @param key Name of the environment variable to require.
   *
   * @param fn A mapping function that accepts the value of the target,
   * non-null, non-blank environment variable and returns a value of type [T].
   *
   * @return A value of type [T] returned by the given mapping function.
   */
  fun <T> require(key: String, fn: (String) -> T): T

  /**
   * Looks up a target environment variable with a name matching the given [key]
   * value, and returns its value mapped to type [T] by the given mapping
   * function ([fn]).
   *
   * If the target environment variable does exist on the environment, but is
   * blank, `null` will be returned and the mapping function will not be called.
   *
   * @param key Name of the environment variable to require.
   *
   * @return The target environment variable, if it is set and non-blank,
   * otherwise `null`.
   */
  fun <T> optional(key: String, fn: (String) -> T): T?

  fun rawEnvironment(): Map<String, String>
}