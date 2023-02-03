plugins {
  kotlin("jvm")
}

kotlin {
  this.jvmToolchain(18)
}

dependencies {
  implementation(project(":components:json"))

  implementation("io.ktor:ktor-server-core-jvm:2.2.2")
  implementation("org.slf4j:slf4j-api:1.7.36")
}