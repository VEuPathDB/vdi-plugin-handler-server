plugins {
  kotlin("jvm")
}

kotlin {
  jvmToolchain(18)
}

dependencies {
  api("io.micrometer:micrometer-registry-prometheus:1.12.5")
}
