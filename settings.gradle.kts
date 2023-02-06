rootProject.name = "vdi-handler-server"

include(
  ":components:common",
  ":components:http-errors",
  ":components:io-utils",
  ":components:json",
  ":components:ldap-lookup",
  ":components:script-execution",
)

include(":service")