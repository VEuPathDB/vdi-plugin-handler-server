plugins {
  kotlin("jvm")
}

kotlin {
  jvmToolchain(18)
}

dependencies {
  implementation(project(":components:common"))
  implementation("com.unboundid:unboundid-ldapsdk:6.0.7")
}