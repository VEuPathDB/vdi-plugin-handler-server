package vdi.components.script

import java.nio.file.Path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScriptExecutorImpl : ScriptExecutor {
  override suspend fun <T> executeScript(
    command:     String,
    workDir:     Path,
    arguments:   Array<String>,
    environment: Map<String, String>,
    fn:          suspend ScriptProcess.() -> T,
  ): T = withContext(Dispatchers.IO) {
    val rawProcess = ProcessBuilder(command, *arguments).apply {
      directory(workDir.toFile())
      environment().clear()
      environment().putAll(environment)
    }.start()

    val out = try {
      fn(ScriptProcessImpl(command, rawProcess))
    } finally {
      rawProcess.waitFor()
    }

    out
  }
}