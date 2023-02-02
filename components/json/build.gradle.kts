plugins {
  kotlin("jvm")
}

kotlin {
  this.jvmToolchain(18)
}

dependencies {
  api("com.fasterxml.jackson.core:jackson-core:2.14.2")
  api("com.fasterxml.jackson.core:jackson-databind:2.14.2")
  api("com.fasterxml.jackson.core:jackson-annotations:2.14.2")
  api("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
  api("com.fasterxml.jackson.module:jackson-module-parameter-names:2.14.2")
  api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.2")
  api("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.14.2")
}