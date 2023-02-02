plugins {
  id("com.github.johnrengelman.shadow") version "7.1.2"
  kotlin("jvm") version "1.8.0"
}

repositories {
  mavenCentral()
}

kotlin {
  this.jvmToolchain(18)
}

dependencies {
  implementation("io.ktor:ktor-server-core-jvm:2.2.2")
  implementation("io.ktor:ktor-server-netty-jvm:2.2.2")
  implementation("io.ktor:ktor-server-metrics-micrometer:2.2.2")

  implementation("org.apache.logging.log4j:log4j-core:2.19.0")
  implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.19.0")
  implementation("org.apache.logging.log4j:log4j-iostreams:2.19.0")

  implementation("io.micrometer:micrometer-registry-prometheus:1.10.3")

  implementation("com.fasterxml.jackson.core:jackson-core:2.14.2")
  implementation("com.fasterxml.jackson.core:jackson-databind:2.14.2")
  implementation("com.fasterxml.jackson.core:jackson-annotations:2.14.2")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
  implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:2.14.2")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.2")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.14.2")

  implementation("org.apache.commons:commons-compress:1.22")

  testImplementation(kotlin("test"))
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
  testImplementation("org.mockito:mockito-core:4.8.0")
}

tasks.test {
  useJUnitPlatform()
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
    val outputFile = file("api.html")
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

tasks.create("docker-build") {
  doLast {
    with(
      ProcessBuilder("docker", "build", "-t", "veupathdb/vdi-handler-server:latest", ".")
        .directory(projectDir)
        .start()
    ) {
      inputStream.transferTo(System.out)
      errorStream.transferTo(System.err)

      if (waitFor() != 0) {
        throw RuntimeException("docker build failed")
      }
    }
  }
}