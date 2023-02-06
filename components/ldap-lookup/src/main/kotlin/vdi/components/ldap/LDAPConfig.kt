package vdi.components.ldap

data class LDAPConfig(val hosts: Collection<LDAPHost>, val oracleBaseDN: String)