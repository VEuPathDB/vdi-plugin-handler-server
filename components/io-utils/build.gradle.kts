plugins {
  kotlin("jvm")
}

kotlin {
  this.jvmToolchain(18)
}

dependencies {
  implementation("org.slf4j:slf4j-api:1.7.36")

  testImplementation(kotlin("test"))
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
  testImplementation("org.mockito:mockito-core:5.2.0")
}