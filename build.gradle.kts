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
    implementation("ws.schild:jave-core:3.4.0")
    implementation("ws.schild:jave-nativebin-linux64:3.4.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
