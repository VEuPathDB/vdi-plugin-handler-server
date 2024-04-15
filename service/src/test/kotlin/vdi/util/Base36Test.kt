package vdi.util

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class Base36Test {
  private data class U64(val input: ULong, val expect: String)

  @Test
  fun encodeToString() {
    val tests = arrayOf(
      U64(35uL, "z"),
      U64(36uL, "10"), // 36^1
      U64(69uL, "1x"),

      U64(420uL, "bo"),
      U64(666uL, "ii"),

      U64(1295uL, "zz"),
      U64(1296uL, "100"), // 36^2
      U64(1988uL, "1j8"),
      U64(1998uL, "1ji"),

      U64(16180uL, "chg"),
      U64(27182uL, "kz2"),
      U64(46655uL, "zzz"),
      U64(46656uL, "1000"), // 36^3

      U64(186282uL, "3zqi"),
      U64(667300uL, "eaw4"),

      U64(1380650uL, "tlbe"),
      U64(1679615uL, "zzzz"),
      U64(1679616uL, "10000"), // 36^4

      U64(12020569uL, "75n4p"),
      U64(60221515uL, "zur7v"),
      U64(60466175uL, "zzzzz"),
      U64(60466176uL, "100000"), // 36^5

      U64(2176782335uL, "zzzzzz"),
      U64(2176782336uL, "1000000"), // 36^6

      U64(78364164095uL, "zzzzzzz"),
      U64(78364164096uL, "10000000"), // 36^7

      U64(2821109907455uL, "zzzzzzzz"),
      U64(2821109907456uL, "100000000"), // 36^8

      U64(101559956668415uL, "zzzzzzzzz"),
      U64(101559956668416uL, "1000000000"), // 36^9

      U64(3656158440062975uL, "zzzzzzzzzz"),
      U64(3656158440062976uL, "10000000000"), // 36^10

      U64(131621703842267135uL, "zzzzzzzzzzz"),
      U64(131621703842267136uL, "100000000000"), // 36^11

      U64(4738381338321616895uL, "zzzzzzzzzzzz"),
      U64(4738381338321616896uL, "1000000000000"), // 36^12

      U64(ULong.MAX_VALUE, "3w5e11264sgsf"),
    )

    for (test in tests) {
      assertEquals(test.expect, Base36.encodeToString(test.input))
    }
  }
}