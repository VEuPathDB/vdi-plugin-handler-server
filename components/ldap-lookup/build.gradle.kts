plugins {
  kotlin("jvm")
}

kotlin {
  jvmToolchain(18)
}

dependencies {
  implementation("org.slf4j:slf4j-api:1.7.36")
  implementation("com.unboundid:unboundid-ldapsdk:6.0.7")
}