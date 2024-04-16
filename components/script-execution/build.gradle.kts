plugins {
  kotlin("jvm")
}

kotlin {
  jvmToolchain(18)
}

dependencies {
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
}
