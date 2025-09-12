plugins {
  application
  java
}

application {
  mainClass = "com.prastavna.leetcode.App"
}

tasks.jar {
  manifest {
    attributes(mapOf("Main-Class" to "com.prastavna.leetcode.App"))
  }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.graphql-java:graphql-java:24.2")
}
