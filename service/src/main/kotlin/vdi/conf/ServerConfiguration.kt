package vdi.conf

import vdi.Const
import vdi.components.common.EnvironmentAccessor

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
  constructor(env: EnvironmentAccessor) : this(
    port = (env.optional(Const.ConfigEnvKey.ServerPort) ?: Const.ConfigDefault.ServerPort).toUShort(),
    host = env.optional(Const.ConfigEnvKey.ServerHost) ?: Const.ConfigDefault.ServerHost,
  )
}
