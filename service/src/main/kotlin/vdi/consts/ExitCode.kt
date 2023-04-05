package vdi.consts

object ExitCode {
  const val ImportScriptSuccess           = 0
  const val ImportScriptValidationFailure = 1

  const val InstallScriptSuccess           = 0
  const val InstallScriptValidationFailure = 1

  const val CompatScriptSuccess      = 0
  const val CompatScriptIncompatible = 1
}