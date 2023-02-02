rootProject.name = "vdi-handler-server"

include(
  ":components:http-errors",
  ":components:io-utils",
  ":components:json",
  ":components:script-execution"
)

include(":service")