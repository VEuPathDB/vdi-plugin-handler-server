package vdi.conf

import vdi.Const

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
  constructor(env: Map<String, String> = System.getenv()) : this(
    port = (env[Const.EnvKey.ServerPort] ?: Const.ConfigDefault.ServerPort).toUShort(),
    host = env[Const.EnvKey.ServerHost] ?: Const.ConfigDefault.ServerHost,
  )
}
