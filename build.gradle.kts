plugins {
    kotlin("jvm") version "1.9.22"
}

group = "top.ant00000ny"
version = "1.0"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("com.github.promeg:tinypinyin:2.0.3")
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.slf4j:slf4j-simple:2.0.9")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
