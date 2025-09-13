plugins {
  application
  java
}

repositories {
    mavenCentral()
}

tasks.jar {
  manifest {
    attributes(mapOf("Main-Class" to "com.prastavna.leetcode.App"))
  }
}

dependencies {
  implementation("com.fasterxml.jackson.core:jackson-core:2.20.0")
  implementation("com.fasterxml.jackson.core:jackson-databind:2.20.0")
}

application {
  mainClass = "com.prastavna.leetcode.App"
}

