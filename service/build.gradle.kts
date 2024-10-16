import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  id("com.github.johnrengelman.shadow") version "7.1.2"
  kotlin("jvm")
}

repositories {
  mavenCentral()
  maven {
    name = "GitHubPackages"
    url  = uri("https://maven.pkg.github.com/veupathdb/maven-packages")
    credentials {
      username = if (extra.has("gpr.user")) extra["gpr.user"] as String? else System.getenv("GITHUB_USERNAME")
      password = if (extra.has("gpr.key")) extra["gpr.key"] as String? else System.getenv("GITHUB_TOKEN")
    }
  }
}

kotlin {
  jvmToolchain(18)
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

  implementation("org.veupathdb.vdi:vdi-component-json:1.0.2")
  implementation("org.veupathdb.vdi:vdi-component-common:12.0.2")

  implementation("io.ktor:ktor-server-core-jvm:2.3.12")
  implementation("io.ktor:ktor-server-netty-jvm:2.3.12")
  implementation("io.ktor:ktor-server-metrics-micrometer:2.3.12")

  implementation("org.slf4j:slf4j-api:1.7.36")
  implementation("org.apache.logging.log4j:log4j-core:2.23.1")
  implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.23.1")
  implementation("org.apache.logging.log4j:log4j-iostreams:2.23.1")

  testImplementation(kotlin("test"))
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.2")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.2")
  testImplementation("org.mockito:mockito-core:5.14.2")
  testImplementation("org.mockito:mockito-junit-jupiter:5.14.2")
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
  exclude("**/Log4j2Plugins.dat")
  archiveFileName.set("service.jar")
}

tasks.create("generate-raml-docs") {
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
