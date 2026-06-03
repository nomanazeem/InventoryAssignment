plugins {
    id("java")
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common-module"))
}

tasks.test {
    useJUnitPlatform()
}