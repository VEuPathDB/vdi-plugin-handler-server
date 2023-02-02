package vdi.components.script

import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScriptExecutorImpl : ScriptExecutor {
  override suspend fun <T> executeScript(
    command:     String,
    workDir:     File,
    arguments:   Array<String>,
    environment: Map<String, String>,
    fn:          ScriptProcess.() -> T,
  ): T = withContext(Dispatchers.IO) {
    val rawProcess = ProcessBuilder(command, *arguments).apply {
      directory(workDir)
      environment().clear()
      environment().putAll(environment)
    }.start()

    val out = fn(ScriptProcessImpl(rawProcess))

    rawProcess.waitFor()

    out
  }
}