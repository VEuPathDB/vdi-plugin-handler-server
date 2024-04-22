plugins {
  kotlin("jvm") version "1.9.23"
}

allprojects {
  repositories {
    mavenCentral()
  }
}


tasks.create("docker-build") {
  doLast {
    with(
      ProcessBuilder("docker", "build", "-t", "veupathdb/vdi-handler-server:latest", ".")
        .directory(rootDir)
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
