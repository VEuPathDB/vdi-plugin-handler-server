package vdi.server.controller

import io.ktor.server.application.*

suspend fun ApplicationCall.handlePostInstallData() {
  // Parse and validate multipart post body
  // validate details json
  // create a temp directory
  // unpack the dataset archive
  // lookup project id in environment database config
  // lookup connection string in LDAP
  // call installer script
  // handle exit code
}