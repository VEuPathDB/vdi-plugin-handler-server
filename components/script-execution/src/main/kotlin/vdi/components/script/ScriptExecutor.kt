package vdi.components.script

import java.nio.file.Path

/**
 * Script Executor
 *
 * Script/process execution manager.
 *
 * This type is used over direct use of Java's [ProcessBuilder] to allow for
 * unit testing of functions that execute command line scripts.
 */
interface ScriptExecutor {

  /**
   * Executes the target command in the target working directory and calls the
   * given function on the running [ScriptProcess] instance.
   *
   * This function waits to return until after the script has exited.
   *
   * **Example**
   * ```kotlin
   * // This sample execution runs "myCommand" in the directory "here" and
   * // requiring the execution of the command to complete within 3600 seconds
   * // then returns "success" or "failure" based on whether the command's exit
   * // code.
   * val result = ScriptExecutor("myCommand", File("here")) {
   *   waitFor(timeoutSeconds = 3600)
   *
   *   return if (exitCode() == 0)
   *     "success"
   *   else
   *     "failure"
   * }
   * ```
   *
   * @param command Name of or path to the command to execute.
   *
   * @param workDir Context directory from which the command will be executed.
   *
   * @param arguments Optional array of additional arguments to pass to the
   * command being executed.
   *
   * @param environment Optional environment variables to set for the command
   * being executed.
   *
   * @param fn Function that is executed on the started [ScriptProcess]
   * instance.  This function will be called once, immediately after the target
   * command is started without regard to whether the command is still running
   * or has already exited.  The function is expected to make that determination
   * itself and act accordingly.
   *
   * @param T Return type of the function that consumes the running process.
   *
   * @return Returns the value returned by the given function.
   */
  suspend fun <T> executeScript(
    command: String,
    workDir: Path,
    arguments: Array<String> = emptyArray(),
    environment: Map<String, String> = emptyMap(),
    fn: suspend ScriptProcess.() -> T
  ): T
}

