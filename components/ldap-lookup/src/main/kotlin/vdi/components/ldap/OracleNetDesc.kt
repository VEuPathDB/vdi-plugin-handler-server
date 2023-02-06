package vdi.components.ldap

import java.math.BigInteger

private const val HOST_PREFIX = "(HOST="
private const val PORT_PREFIX = "(PORT="
private const val SERVICE_NAME_PREFIX = "(SERVICE_NAME="
private const val VALUE_SUFFIX = ')'

data class OracleNetDesc(
  val host: String,
  val port: UShort,
  val serviceName: String,
) {
  constructor(string: String) : this(
    string.requireHostValue(),
    string.requirePortValue(),
    string.requireServiceNameValue(),
  )
}

private fun String.requireHostValue() = requireValue(HOST_PREFIX, "HOST")

private fun String.requirePortValue(): UShort {
  val bi = try {
    BigInteger(requireValue(PORT_PREFIX, "PORT"))
  } catch (e: Throwable) {
    throw IllegalArgumentException("given orclNetDescString contained an invalid PORT value")
  }

  if (bi > BigInteger.valueOf(65535))
    throw IllegalArgumentException("given orclNetDescString contained a PORT value that was too large to be a valid port")
  if (bi < BigInteger.ZERO)
    throw IllegalArgumentException("given orclNetDescString contained a PORT value that was less than zero")

  return bi.toInt().toUShort()
}

private fun String.requireServiceNameValue(): String = requireValue(SERVICE_NAME_PREFIX, "SERVICE_NAME")

private fun String.requireValue(prefix: String, name: String): String {
  val start = indexOf(prefix)

  if (start < 0)
    throw IllegalArgumentException("given orclNetDescString did not contain a $name value")

  val end = indexOf(VALUE_SUFFIX, start)

  if (end < 0)
    throw IllegalArgumentException("malformed orclNetDescString value")

  val out = substring(start + prefix.length, end)

  if (out.isEmpty())
    throw IllegalArgumentException("given orclNetDescString contained an empty $name value")

  return out
}
