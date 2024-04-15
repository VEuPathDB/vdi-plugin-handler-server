package vdi.util

object Base36 {
  /**
   * The maximum width of an unsigned int64 value in base-36 is 13 digits:
   * `3w5e11264sg0g`.
   *
   * The maximum base-36 value possible for a 15 digit sequences is
   * `170,581,728,179,578,208,256`.
   */
  private const val MaxI64Width = 13

  private const val UL36 = 36uL

  /**
   * Base 36 alphabet.
   *
   * ```
   * 0 1 2 3 4 5 6 7 8 9
   * a b c d e f g h i j
   * k l m n o p q r s t
   * u v w x y z
   * ```
   */
  private val alpha = ByteArray(36) { if (it < 10) (it + 48).toByte() else (it + 87).toByte() }

  /**
   * Encodes the given int64 value as a base-36 string.
   *
   * @param value Int64 value to encode.
   *
   * @return Base-36 string representation of the input value.
   */
  @JvmStatic
  fun encodeToString(value: ULong): String {
    // mutable copy of the input value
    var mutVal = value

    val tmp = ByteArray(MaxI64Width)

    // work backwards from the last index to put the digits in the correct
    // order.
    var idx = MaxI64Width - 1

    while (mutVal > UL36) {
      tmp[idx--] = alpha[(mutVal % UL36).toInt()]
      mutVal /= UL36
    }

    // Handle the case where the input value was a multiple of 36
    if (mutVal == UL36) {
      tmp[idx--] = alpha[0]
      tmp[idx] = alpha[1]
    } else {
      tmp[idx] = alpha[mutVal.toInt()]
    }

    // slice out the part of the temp array we actually used and convert to
    // string
    return tmp.sliceArray(idx ..< MaxI64Width)
      .toString(Charsets.US_ASCII)
  }
}