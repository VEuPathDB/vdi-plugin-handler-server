package vdi.util

private const val VDI_ID_LENGTH = 32

/**
 * Requires that the given input ID string is a valid VDI ID.
 *
 * If the given input string is _not_ a valid VDI ID, this method returns
 * `null`.
 *
 * If the given input string _is_ a valid VDI ID, this method returns the given
 * input value.
 *
 * @param id ID string to test.
 *
 * @return The given input string ([id]) if the value appears to be a valid VDI
 * ID, otherwise `null`.
 */
fun requireValidVDIID(id: String): String? {
  // If the length of the given input string is _not_ equal to the expected
  // valid length of a VDI ID
  if (id.length != VDI_ID_LENGTH)
    // Then return `null`
    return null

  // Iterate over all the characters in the input string.
  for (c in id) {
    // When the current character in the input string is
    when (c) {
      // in the character range 0..9, continue because that is expected
      in '0' .. '9' -> continue
      // in the character range A..F, continue because that is expected
      in 'A' .. 'F' -> continue
      // in the character range a..f, continue because that is expected
      in 'a' .. 'f' -> continue
      // not in any above ranges, return null because it's an invalid character
      else          -> return null
    }
  }

  // If we made it this far, then we have a string of the correct length which
  // consists entirely of valid characters.
  return id
}