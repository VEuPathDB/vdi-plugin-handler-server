plugins {
  kotlin("jvm")
}

kotlin {
  jvmToolchain(18)
}

dependencies {
  implementation("io.micrometer:micrometer-registry-prometheus:1.10.3")
}
