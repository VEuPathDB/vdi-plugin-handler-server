package vdi.conf

import org.junit.jupiter.api.*
import org.veupathdb.vdi.lib.common.env.EnvKey
import vdi.model.DBPlatform
import kotlin.test.assertEquals

@DisplayName("DatabaseConfigurationMap")
class DatabaseConfigurationMapTest {

  @Nested
  @DisplayName("constructor")
  inner class Constructor {

    @Nested
    @DisplayName("given zero entries")
    inner class Zero {

      @Test
      @DisplayName("throws exception")
      fun t1() {
        assertThrows<IllegalStateException> { DatabaseConfigurationMap(emptyMap()) }
      }
    }

    @Nested
    @DisplayName("given one entry")
    inner class One {

      @Nested
      @DisplayName("that is disabled")
      inner class NoneEnabled {

        @Test
        @DisplayName("throws exception")
        fun t1() {
          val env = makeLDAPEntry(key = "SOMETHING", enabled = false)

          assertThrows<IllegalStateException> { DatabaseConfigurationMap(env) }
        }
      }

      @Nested
      @DisplayName("that is valid")
      inner class Valid {

        @Nested
        @DisplayName("for an LDAP connection")
        inner class LDAP {

          @Test
          @DisplayName("does not throw")
          fun t1() {
            assertDoesNotThrow { DatabaseConfigurationMap(makeLDAPEntry("SOMETHING")) }
          }

          @Test
          @DisplayName("correctly translates env var strings to typed values")
          fun t2() {
            val configMap = DatabaseConfigurationMap(makeLDAPEntry(
              key = "SOMETHING",
              platform = "oracle", // this is the only non-string typed var for LDAP configs
            ))

            val config = configMap.values.first()

            assertEquals(DBPlatform.Oracle, config.platform)
          }
        }

        @Nested
        @DisplayName("for a manual (non-LDAP) connection")
        inner class Manual {

          @Test
          @DisplayName("does not throw")
          fun t1() {
            assertDoesNotThrow { DatabaseConfigurationMap(makeManualEntry("SOMETHING")) }
          }

          @Test
          @DisplayName("correctly translates env var strings to typed values")
          fun t2() {
            val configMap = DatabaseConfigurationMap(makeManualEntry(
              key = "SOMETHING",
              port = 1521u,
              platform = "oracle",
            ))

            val config = configMap.values.first()

            assertEquals(DBPlatform.Oracle, config.platform)
            assertEquals(1521.toUShort(), config.port)
          }
        }
      }

      @Nested
      @DisplayName("that is invalid")
      inner class Invalid {

        @Nested
        @DisplayName("due to having no connection name")
        inner class NoConName {

          @Test
          @DisplayName("throws exception")
          fun t1() {
            val env = makeLDAPEntry("SOMETHING")
              .also { it.remove(EnvKey.AppDB.DBConnectionNamePrefix + "SOMETHING") }

            assertThrows<IllegalStateException> { DatabaseConfigurationMap(env) }
          }
        }

        @Nested
        @DisplayName("due to having no data schema")
        inner class NoSchema {

          @Test
          @DisplayName("throws exception")
          fun t1() {
            val env = makeLDAPEntry("SOMETHING")
              .also { it.remove(EnvKey.AppDB.DBDataSchemaPrefix + "SOMETHING") }

            assertThrows<IllegalStateException> { DatabaseConfigurationMap(env) }
          }
        }

        @Nested
        @DisplayName("due to having no password")
        inner class NoPassword {

          @Test
          @DisplayName("throws exception")
          fun t1() {
            val env = makeLDAPEntry("SOMETHING")
              .also { it.remove(EnvKey.AppDB.DBPassPrefix + "SOMETHING") }

            assertThrows<IllegalStateException> { DatabaseConfigurationMap(env) }
          }
        }

        @Nested
        @DisplayName("due to having an invalid platform")
        inner class BadPlatform {

          @Test
          @DisplayName("throws exception")
          fun t1() {
            val env = makeLDAPEntry("SOMETHING", platform = "hello")

            assertThrows<IllegalArgumentException> { DatabaseConfigurationMap(env) }
          }
        }

        @Nested
        @DisplayName("due to having no ldap key")
        inner class NoLDAP {

          @Nested
          @DisplayName("and no manual connection details")
          inner class NoManualDetails {

            @Test
            @DisplayName("throws exception")
            fun t1() {
              val env = makeLDAPEntry("SOMETHING")
                .also { it.remove(EnvKey.AppDB.DBLDAPPrefix + "SOMETHING") }

              assertThrows<IllegalStateException> { DatabaseConfigurationMap(env) }
            }
          }

          @Nested
          @DisplayName("and incomplete manual connection details")
          inner class IncompleteManualDetails {

            @Nested
            @DisplayName("due to missing db host")
            inner class MissingHost {
              @Test
              @DisplayName("throws exception")
              fun t1() {
                val env = makeManualEntry("SOMETHING")
                  .also { it.remove(EnvKey.AppDB.DBHostPrefix + "SOMETHING") }

                assertThrows<IllegalStateException> { DatabaseConfigurationMap(env) }
              }
            }

            @Nested
            @DisplayName("due to missing db port")
            inner class MissingPort {
              @Test
              @DisplayName("throws exception")
              fun t1() {
                val env = makeManualEntry("SOMETHING")
                  .also { it.remove(EnvKey.AppDB.DBPortPrefix + "SOMETHING") }

                assertThrows<IllegalStateException> { DatabaseConfigurationMap(env) }
              }
            }

            @Nested
            @DisplayName("due to missing db name")
            inner class MissingName {
              @Test
              @DisplayName("throws exception")
              fun t1() {
                val env = makeManualEntry("SOMETHING")
                  .also { it.remove(EnvKey.AppDB.DBNamePrefix + "SOMETHING") }

                assertThrows<IllegalStateException> { DatabaseConfigurationMap(env) }
              }
            }
          }
        }
      }
    }
  }

  private fun makeLDAPEntry(
    key: String,
    enabled: Boolean = true,
    name: String = key,
    schema: String = "schema",
    password: String = "password",
    ldap: String = "record",
    platform: String? = null,
  ) = mutableMapOf(
    EnvKey.AppDB.DBEnabledPrefix + key        to enabled.toString(),
    EnvKey.AppDB.DBConnectionNamePrefix + key to name,
    EnvKey.AppDB.DBDataSchemaPrefix + key     to schema,
    EnvKey.AppDB.DBPassPrefix + key           to password,
    EnvKey.AppDB.DBLDAPPrefix + key           to ldap,
  ).also { if (platform != null) it[EnvKey.AppDB.DBPlatformPrefix + key] = platform }

  private fun makeManualEntry(
    key: String,
    enabled: Boolean = true,
    name: String = key,
    schema: String = "schema",
    password: String = "password",
    host: String = "somehost",
    dbName: String = "somedb",
    port: UShort = 1521u,
    platform: String? = null,
  ) = mutableMapOf(
    EnvKey.AppDB.DBEnabledPrefix + key        to enabled.toString(),
    EnvKey.AppDB.DBConnectionNamePrefix + key to name,
    EnvKey.AppDB.DBDataSchemaPrefix + key     to schema,
    EnvKey.AppDB.DBPassPrefix + key           to password,
    EnvKey.AppDB.DBHostPrefix + key           to host,
    EnvKey.AppDB.DBNamePrefix + key           to dbName,
    EnvKey.AppDB.DBPortPrefix + key           to port.toString(),
  ).also { if (platform != null) it[EnvKey.AppDB.DBPlatformPrefix + key] = platform }
}
