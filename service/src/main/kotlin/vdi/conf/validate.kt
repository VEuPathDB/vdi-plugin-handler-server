package vdi.conf

import java.io.File

fun HandlerConfig.validate() {

  validateScript(service.importScript.path)
  validateScript(service.installDataScript.path)
  validateScript(service.installMetaScript.path)
  validateScript(service.uninstallScript.path)

  if (databases.isEmpty())
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