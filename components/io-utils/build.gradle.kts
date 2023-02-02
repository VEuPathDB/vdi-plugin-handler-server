plugins {
  kotlin("jvm")
}

kotlin {
  this.jvmToolchain(18)
}

dependencies {
  implementation("org.slf4j:slf4j-api:2.0.6")

  testImplementation(kotlin("test"))
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
  testImplementation("org.mockito:mockito-core:4.8.0")
}