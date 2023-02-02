package vdi.util

/**
 * Represents a string containing a "secret" which should not be printed in
 * standard [toString] calls.
 *
 * @constructor Constructs a new `SecretString` instance.
 *
 * @param value Value of the `SecretString`.
 *
 * @author Elizabeth Paige Harper - https://github.com/foxcapades
 * @since 1.0.0
 */
@JvmInline
value class SecretString(val value: String) {
  override fun toString(): String {
    return "***"
  }
}