package vdi.components.script

import java.io.InputStream
import java.io.OutputStream
import kotlin.time.Duration

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
   */
  fun waitFor(timeout: Duration)

  /**
   * Returns the exit code of the process.
   */
  fun exitCode(): Int

  /**
   * Tests whether the process is still alive.
   */
  fun isAlive(): Boolean
}
