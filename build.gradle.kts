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
  implementation("com.openai:openai-java:3.5.2")
  implementation("io.github.cdimascio:dotenv-java:3.2.0")
}

application {
  mainClass = "com.prastavna.leetcode.App"
}

