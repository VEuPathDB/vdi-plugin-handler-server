package vdi.server

import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import vdi.components.http.errors.withExceptionMapping
import vdi.model.ApplicationContext
import vdi.server.controller.*

fun Application.configureServer(appCtx: ApplicationContext) {

  install(MicrometerMetrics) { registry = appCtx.metrics.micrometer }

  routing {
    post("/import") { withExceptionMapping { call.handleImportRequest(appCtx) } }

    route("/install") {
      post("/data") { withExceptionMapping { call.handleInstallDataRequest(appCtx) } }
      post("/meta") { withExceptionMapping { call.handleInstallMetaRequest(appCtx) } }
    }

    post("/uninstall") { withExceptionMapping { call.handleUninstallRequest(appCtx) } }

    get("/metrics") { call.respond(appCtx.metrics.micrometer.scrape()) }
  }
}
