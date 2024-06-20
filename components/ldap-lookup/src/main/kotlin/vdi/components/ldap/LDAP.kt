package vdi.components.ldap

import com.unboundid.ldap.sdk.*
import org.slf4j.LoggerFactory

class LDAP(private val config: LDAPConfig) {
  private val log = LoggerFactory.getLogger(javaClass)

  private var ldapConnection: LDAPConnection? = null

  init {
    if (config.hosts.isEmpty())
      throw IllegalArgumentException("Passed the $javaClass constructor a config with 0 hosts entries")
  }

  fun requireSingularOracleNetDesc(commonName: String): OracleNetDesc {
    log.trace("requireSingularOracleNetDesc(commonName={})", commonName)

    val tmp = lookupOracleNetDesc(commonName)

    if (tmp.isEmpty())
      throw IllegalStateException("no OracleNetDescs found for common name $commonName")
    if (tmp.size > 1)
      throw IllegalStateException("multiple OracleNetDescs found for common name $commonName")

    return tmp[0]
  }

  fun lookupOracleNetDesc(commonName: String): List<OracleNetDesc> {
    log.trace("lookupOracleNetDesc(commonName={})", commonName)

    var err: LDAPSearchException? = null

    // To handle network hiccups, try this 5x before giving up.
    for (i in 0..5) {
      try {
        return getConnection()
          .search(
            SearchRequest(
              config.oracleBaseDN,
              SearchScope.SUB,
              Filter.createANDFilter(
                Filter.create("cn=$commonName"),
                Filter.create("objectClass=orclNetService")
              ),
              "orclNetDescString"
            )
          )
          .searchEntries
          .map { OracleNetDesc(it.getAttribute("orclNetDescString").value!!) }
      } catch (e: LDAPSearchException) {
        log.warn("failed to search LDAP {} times", i+1)
        err = e
        continue
      }
    }

    throw err!!
  }

  private fun getConnection(): LDAPConnection {
    log.trace("getConnection()")

    // Synchronized because this thing is gonna be called from who knows where.
    synchronized(this) {

      // If we've already got an LDAP connection
      if (ldapConnection != null) {

        // If the LDAP connection we've already got is still connected
        if (ldapConnection!!.isConnected)
          // then return it
          return ldapConnection!!
        // else, the LDAP connection we've already got is _not_ still connected
        else
          try {
            ldapConnection!!.reconnect()
            return ldapConnection!!
          } catch (e: Exception) {
            ldapConnection = null
          }
      }

      log.debug("Attempting to establish a connection to a configured LDAP server")
      for (host in config.hosts) {
        log.trace("Trying to connect to {}:{}", host.host, host.port)

         try {
           ldapConnection = LDAPConnection(host.host, host.port.toInt())
            .also { log.debug("Connected to {}:{}", host.host, host.port) }
          break
        } catch (e: Throwable) {
          log.debug("Failed to connect to {}:{}", host.host, host.port)
        }
      }

      if (ldapConnection == null) {
        log.error("Failed to establish a connection to any configured LDAP server.")
        throw RuntimeException("Failed to establish a connection to any configured LDAP server.")
      }

      return ldapConnection!!
    }
  }
}