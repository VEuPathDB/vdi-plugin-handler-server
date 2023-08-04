package vdi.conf

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import vdi.components.common.EnvironmentAccessor

@DisplayName("DatabaseConfigurationMap")
class TestDatabaseConfigurationMap {

  private lateinit var mockEnv: EnvironmentAccessor

  @BeforeEach
  fun setup() {
    mockEnv = Mockito.mock(EnvironmentAccessor::class.java)
  }

  @Test
  @DisplayName("Success Case")
  fun t1() {
    val input = mapOf(
      "DB_CONNECTION_NAME_PLASMO_DB" to "PlasmoDB",
      "DB_CONNECTION_LDAP_PLASMO_DB" to "foo",
      "DB_CONNECTION_USER_PLASMO_DB" to "bar",
      "DB_CONNECTION_PASS_PLASMO_DB" to "fizz",
      "DB_CONNECTION_SCHEMA_PLASMO_DB" to "bazz",

      "DB_CONNECTION_NAME_TOXO_DB" to "ToxoDB",
      "DB_CONNECTION_LDAP_TOXO_DB" to "buzz",
      "DB_CONNECTION_USER_TOXO_DB" to "ding",
      "DB_CONNECTION_PASS_TOXO_DB" to "dong",
      "DB_CONNECTION_SCHEMA_TOXO_DB" to "dang",
    )

    Mockito.`when`(mockEnv.rawEnvironment()).thenReturn(input)

    val output = DatabaseConfigurationMap(mockEnv)

    assertEquals(2, output.size)
    assertTrue("PlasmoDB" in output)
    assertTrue("ToxoDB" in output)

    assertEquals("PlasmoDB", output["PlasmoDB"]!!.name)
    assertEquals("foo", output["PlasmoDB"]!!.ldap)
    assertEquals("bar", output["PlasmoDB"]!!.user)
    assertEquals("fizz", output["PlasmoDB"]!!.pass.value)
    assertEquals("bazz", output["PlasmoDB"]!!.schema)

    assertEquals("ToxoDB", output["ToxoDB"]!!.name)
    assertEquals("buzz", output["ToxoDB"]!!.ldap)
    assertEquals("ding", output["ToxoDB"]!!.user)
    assertEquals("dong", output["ToxoDB"]!!.pass.value)
    assertEquals("dang", output["ToxoDB"]!!.schema)
  }

  @Test
  @DisplayName("Fails when group name is missing")
  fun t2() {
    val input = mapOf(
      "DB_CONNECTION_LDAP_PLASMO_DB" to "foo",
      "DB_CONNECTION_USER_PLASMO_DB" to "bar",
      "DB_CONNECTION_PASS_PLASMO_DB" to "fizz",
      "DB_CONNECTION_SCHEMA_PLASMO_DB" to "bazz",
    )

    Mockito.`when`(mockEnv.rawEnvironment()).thenReturn(input)

    assertThrows<RuntimeException> { DatabaseConfigurationMap(mockEnv) }
  }

  @Test
  @DisplayName("Fails when group ldap is missing")
  fun t3() {
    val input = mutableMapOf(
      "DB_CONNECTION_NAME_PLASMO_DB" to "PlasmoDB",
      "DB_CONNECTION_USER_PLASMO_DB" to "bar",
      "DB_CONNECTION_PASS_PLASMO_DB" to "fizz",
      "DB_CONNECTION_SCHEMA_PLASMO_DB" to "bazz"
    )

    Mockito.`when`(mockEnv.rawEnvironment()).thenReturn(input)

    assertThrows<RuntimeException> { DatabaseConfigurationMap(mockEnv) }
  }

  @Test
  @DisplayName("Fails when group user name is missing")
  fun t4() {
    val input = mapOf(
      "DB_CONNECTION_NAME_PLASMO_DB" to "PlasmoDB",
      "DB_CONNECTION_LDAP_PLASMO_DB" to "foo",
      "DB_CONNECTION_PASS_PLASMO_DB" to "fizz",
      "DB_CONNECTION_SCHEMA_PLASMO_DB" to "bazz",
    )

    Mockito.`when`(mockEnv.rawEnvironment()).thenReturn(input)

    assertThrows<RuntimeException> { DatabaseConfigurationMap(mockEnv) }
  }

  @Test
  @DisplayName("Fails when group password is missing")
  fun t5() {
    val input = mapOf(
      "DB_CONNECTION_NAME_PLASMO_DB" to "PlasmoDB",
      "DB_CONNECTION_LDAP_PLASMO_DB" to "foo",
      "DB_CONNECTION_USER_PLASMO_DB" to "bar",
      "DB_CONNECTION_SCHEMA_PLASMO_DB" to "fizz"
    )

    Mockito.`when`(mockEnv.rawEnvironment()).thenReturn(input)

    assertThrows<RuntimeException> { DatabaseConfigurationMap(mockEnv) }
  }

  // TODO: Move this test to the validate unit tests
//  @Test
//  @DisplayName("Fails when no db groups are present")
//  fun t6() {
//    val input = mutableMapOf<String, String>()
//
//    assertThrows<RuntimeException> { DatabaseConfigurationMap(input) }
//  }

}