package vdi.conf

import org.veupathdb.vdi.lib.common.env.Environment
import org.veupathdb.vdi.lib.common.env.optional
import vdi.consts.ConfigDefault
import vdi.consts.ConfigEnvKey

/**
 * HTTP Server Specific Configuration Options
 *
 * @author Elizabeth Paige Harper - https://github.com/foxcapades
 * @since 1.0.0
 */
data class ServerConfiguration(
  val port: UShort,
  val host: String
) {
  constructor(env: Environment) : this(
    port = (env.optional(ConfigEnvKey.ServerPort) ?: ConfigDefault.ServerPort).toUShort(),
    host = env.optional(ConfigEnvKey.ServerHost) ?: ConfigDefault.ServerHost,
  )
}
