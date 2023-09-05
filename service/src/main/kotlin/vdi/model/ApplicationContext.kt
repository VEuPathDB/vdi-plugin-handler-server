package vdi.model

import vdi.components.ldap.LDAP
import vdi.components.script.ScriptExecutor
import vdi.conf.HandlerConfig
import vdi.util.DatasetPathFactory

data class ApplicationContext(
  val config:   HandlerConfig,
  val ldap:     LDAP,
  val executor: ScriptExecutor,
  val metrics:  MetricsBundle,
  val pathFactory: DatasetPathFactory
)
