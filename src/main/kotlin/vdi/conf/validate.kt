package vdi.conf

import java.io.File

fun Configuration.validate() {

  validateScript(ServiceConfiguration.importScriptPath)
  validateScript(ServiceConfiguration.installDataScriptPath)
  validateScript(ServiceConfiguration.installMetaScriptPath)
  validateScript(ServiceConfiguration.uninstallScriptPath)

  if (DatabaseConfigurations.isEmpty())
    throw RuntimeException("At least one set of database connection details must be provided.")
}

private fun validateScript(path: String) {
  val file = File(path)

  if (!file.exists())
    throw RuntimeException("target plugin script $file does not exist")
  if (!file.isFile)
    throw RuntimeException("target plugin script $file is not a file")
  if (!file.canExecute())
    throw RuntimeException("target plugin script $file is not marked as executable")
}