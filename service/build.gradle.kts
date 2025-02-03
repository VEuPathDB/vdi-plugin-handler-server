import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  id("com.gradleup.shadow") version "8.3.6"
  kotlin("jvm")
  `maven-publish`
}

configurations.all {
  resolutionStrategy.cacheChangingModulesFor(5, TimeUnit.MINUTES)
}

dependencies {
  implementation(project(":components:http-errors"))
  implementation(project(":components:io-utils"))
  implementation(project(":components:ldap-lookup"))
  implementation(project(":components:metrics"))
  implementation(project(":components:script-execution"))

  implementation(libs.vdi.component.common)
  implementation(libs.vdi.component.json)

  implementation(libs.ktor.core)
  implementation(libs.ktor.netty)
  implementation(libs.ktor.metrics)

  implementation(libs.slf4j.api)
  implementation(libs.log4j.core)
  implementation(libs.log4j.slf4j)
  implementation(libs.log4j.iostreams)

  testImplementation(kotlin("test"))
  testImplementation(libs.junit.api)
  testRuntimeOnly(libs.junit.engine)
  testImplementation(libs.mockito.core)
  testImplementation(libs.mockito.junit)
}

tasks.test {
  useJUnitPlatform()

  testLogging {
    events = setOf(
      TestLogEvent.SKIPPED,
      TestLogEvent.PASSED,
      TestLogEvent.FAILED,
    )
  }
}

tasks.jar {
  manifest {
    attributes(mapOf(
      "Main-Class" to "vdi.MainKt"
    ))
  }
}

tasks.shadowJar {
  exclude(
    "**/Log4j2Plugins.dat"
  )
  archiveFileName.set("service.jar")
  archiveClassifier.set("")
}

tasks.register("generate-raml-docs") {
  doLast {
    val outputFile = rootDir.resolve("docs/http-api.html")
    outputFile.delete()
    outputFile.createNewFile()

    outputFile.outputStream().use { out ->
      with(
        ProcessBuilder(
          "raml2html",
          "api.raml",
          "--theme", "raml2html-modern-theme"
        )
          .directory(projectDir)
          .start()
      ) {
        inputStream.transferTo(out)
        errorStream.transferTo(System.err)

        if (waitFor() != 0) {
          throw RuntimeException("raml2html process failed")
        }
      }
    }
  }
}

val shadowArtifact = artifacts.add("shadow", tasks.shadowJar)

publishing {
  repositories {
    maven {
      name = "GitHub"
      url = uri("https://maven.pkg.github.com/VEuPathDB/vdi-plugin-handler-server")
      credentials {
        username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
        password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
      }
    }
  }

  publications {

    create<MavenPublication>("gpr") {
      artifact(shadowArtifact)

      pom {
        name.set("vdi-plugin-handler-server")
        description.set(project.description)
        url.set("https://github.com/VEuPathDB/vdi-plugin-handler-server")

        licenses {
          license {
            name.set("Apache-2.0")
          }
        }

        developers {
          developer {
            id.set("epharper")
            name.set("Elizabeth Paige Harper")
            email.set("elizabeth.harper@foxcapades.io")
            url.set("https://github.com/foxcapades")
          }
        }

        scm {
          connection.set("scm:git:git://github.com/VEuPathDB/vdi-plugin-handler-server.git")
          developerConnection.set("scm:git:ssh://github.com/VEuPathDB/vdi-plugin-handler-server.git")
          url.set("https://github.com/VEuPathDB/vdi-plugin-handler-server")
        }
      }
    }
  }
}
