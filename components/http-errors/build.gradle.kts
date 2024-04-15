plugins {
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
  this.jvmToolchain(18)
}

dependencies {
  implementation("org.veupathdb.vdi:vdi-component-json:1.0.2")

  implementation("io.ktor:ktor-server-core-jvm:2.3.10")
  implementation("org.slf4j:slf4j-api:1.7.36")
}