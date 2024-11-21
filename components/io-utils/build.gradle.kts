dependencies {
  implementation(libs.slf4j.api)

  testImplementation(kotlin("test"))
  testImplementation(libs.junit.api)
  testRuntimeOnly(libs.junit.engine)
  testImplementation(libs.mockito.core)
}

tasks.test {
  useJUnitPlatform()
}
