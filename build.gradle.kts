import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  kotlin("jvm") version "2.1.10"
}

allprojects {
  repositories {
    mavenCentral()
  }

  apply(plugin = "org.jetbrains.kotlin.jvm")

  kotlin {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_21)
    }
  }

  java {
    targetCompatibility = JavaVersion.VERSION_21
  }

  repositories {
    mavenLocal()
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
