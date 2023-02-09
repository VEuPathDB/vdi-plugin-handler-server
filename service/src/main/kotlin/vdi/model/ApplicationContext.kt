package vdi.model

import vdi.components.ldap.LDAP
import vdi.components.script.ScriptExecutor
import vdi.conf.HandlerConfig

data class ApplicationContext(
  val config:   HandlerConfig,
  val ldap:     LDAP,
  val executor: ScriptExecutor,
  val metrics:  MetricsBundle,
)
