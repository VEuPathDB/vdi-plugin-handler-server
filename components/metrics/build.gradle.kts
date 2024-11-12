plugins {
  kotlin("jvm")
}

kotlin {
  jvmToolchain {
    languageVersion = JavaLanguageVersion.of(21)
    vendor = JvmVendorSpec.AMAZON
  }
}

dependencies {
  api("io.micrometer:micrometer-registry-prometheus:1.12.5")
}
