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
  implementation("org.slf4j:slf4j-api:1.7.36")
  implementation("com.unboundid:unboundid-ldapsdk:6.0.11")
}
