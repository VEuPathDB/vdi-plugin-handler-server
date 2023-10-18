package vdi.conf

/**
 * VDI Handler Service Root Configuration
 *
 * This configuration object is a global container for the configuration values
 * set on the environment for the VDI handler service.
 *
 * @author Elizabeth Paige Harper - https://github.com/foxcapades
 * @since 1.0.0
 */
data class HandlerConfig(

  /**
   * HTTP server specific configuration values.
   */
  val server: ServerConfiguration = ServerConfiguration(System.getenv()),

  /**
   * Handler service functionality configuration values.
   */
  val service: ServiceConfiguration = ServiceConfiguration(System.getenv()),

  /**
   * Database connection configuration values.
   */
  val databases: DatabaseConfigurationMap = DatabaseConfigurationMap(System.getenv()),
)

