package vdi.components.script

import java.io.InputStream
import java.io.OutputStream

/**
 * Script Process
 *
 * Active or executed script process wrapper.
 *
 * This type is used over direct use of Java's [Process] type to allow for unit
 * testing of functions that execute command line scripts.
 */
interface ScriptProcess {

  /**
   * The executed script's STDOUT stream.
   */
  val scriptStdOut: InputStream

  /**
   * The executed script's STDERR stream.
   */
  val scriptStdErr: InputStream

  /**
   * The executed script's STDIN stream.
   */
  val scriptStdIn: OutputStream

  /**
   * Requires that the script completes within the given duration (in seconds).
   *
   * If the script does not complete within the given duration, this method will
   * forcibly kill the script and throw an exception.
   *
   * @param timeoutSeconds Number of seconds that the script execution must
   * complete within.
   *
   * This parameter defaults to `-1`, which means if no timeout.
   */
  fun waitFor(timeoutSeconds: Long = -1)

  /**
   * Returns the exit code of the process.
   */
  fun exitCode(): Int
}