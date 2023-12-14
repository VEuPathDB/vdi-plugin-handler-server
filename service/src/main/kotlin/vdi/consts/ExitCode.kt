package vdi.consts

object ExitCode {
  const val ImportScriptSuccess           = 0
  const val ImportScriptValidationFailure = 99

  const val InstallScriptSuccess           = 0
  const val InstallScriptValidationFailure = 99

  const val CompatScriptSuccess      = 0
  const val CompatScriptIncompatible = 1
}