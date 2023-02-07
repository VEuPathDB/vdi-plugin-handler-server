package vdi.components.common

fun String?.blankToNull() =
  if (this != null && this.isBlank())
    null
  else
    this
