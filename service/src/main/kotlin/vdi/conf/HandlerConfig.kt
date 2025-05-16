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
   * Handler service functionality configuration values.
   */
  val service: ServiceConfiguration,

  /**
   * Database connection configuration values.
   */
  val databases: DatabaseConfigurationMap,
) {
  constructor(raw: RawServiceConfiguration): this(
    service   = ServiceConfiguration(raw),
    databases = DatabaseConfigurationMap(raw),
  )
}

