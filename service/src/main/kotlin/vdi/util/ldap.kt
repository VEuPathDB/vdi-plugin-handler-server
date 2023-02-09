package vdi.util

import vdi.components.common.StartupException
import vdi.components.ldap.LDAP
import vdi.components.ldap.LDAPConfig
import vdi.components.ldap.LDAPHost
import vdi.conf.ServiceConfiguration


private val URLPattern = Regex("^([^:]+):(\\d+)$")

fun setupLDAP(conf: ServiceConfiguration) = LDAP(LDAPConfig(parseLDAPHosts(conf.ldapServer), conf.oracleBaseDN))

private fun parseLDAPHosts(string: String) = string.split(',').map(::parseLDAPHost)

private fun parseLDAPHost(string: String): LDAPHost {
  val match = URLPattern.find(string) ?: throw StartupException("invalid LDAP server entry")

  val (host, port) = match.destructured

  return LDAPHost(host, port.toUShort())
}