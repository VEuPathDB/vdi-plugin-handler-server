package vdi.conf

import vdi.components.common.Env

/**
 * VDI Handler Service Root Configuration
 *
 * This configuration object is a global container for the configuration values
 * set on the environment for the VDI handler service.
 *
 * @author Elizabeth Paige Harper - https://github.com/foxcapades
 * @since 1.0.0
 */
object Configuration {

  /**
   * HTTP server specific configuration values.
   */
  val ServerConfiguration = ServerConfiguration(Env)

  /**
   * Handler service functionality configuration values.
   */
  val ServiceConfiguration = ServiceConfiguration(Env)

  /**
   * Database connection configuration values.
   */
  val DatabaseConfigurations = DatabaseConfigurationMap(Env)
}

