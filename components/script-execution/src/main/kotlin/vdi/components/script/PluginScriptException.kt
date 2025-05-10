package vdi.components.script

import java.lang.Exception

class PluginScriptException : Exception {
  val script: PluginScript

  constructor(script: PluginScript, message: String): super("$script: $message") {
    this.script = script
  }

  constructor(script: PluginScript, message: String, cause: Throwable): super("$script: $message", cause) {
    this.script = script
  }

  constructor(script: PluginScript, cause: Throwable): this(script, cause.message ?: "<no message>", cause)
}
