package vdi.server

import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import vdi.server.controller.handlePostImport
import vdi.server.controller.handlePostInstallMeta
import vdi.server.controller.handlePostUninstall
import vdi.components.http.errors.withExceptionMapping
import vdi.components.ldap.LDAP
import vdi.components.script.ScriptExecutorImpl
import vdi.conf.Configuration
import vdi.server.controller.PostInstallDataController


fun Application.configureRouting(ldap: LDAP) {
  val micrometer = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

  install(MicrometerMetrics) { registry = micrometer }

  routing {
    post("/import") {
      withExceptionMapping {
        call.handlePostImport()
      }
    }

    route("/install") {
      post("/meta") {
        withExceptionMapping {
          call.handlePostInstallMeta()
        }
      }

      post("/data") {
        withExceptionMapping {
          PostInstallDataController(ldap, ScriptExecutorImpl(), Configuration.DatabaseConfigurations)
            .handlePostInstallData(call)
        }
      }
    }

    post("/uninstall") {
      withExceptionMapping {
        call.handlePostUninstall()
      }
    }

    get("/metrics") {
      call.respond(micrometer.scrape())
    }
  }
}
