plugins {
    kotlin("jvm") version "1.9.0" // Исправлено: синхронизация версий
    kotlin("plugin.serialization") version "1.9.0" // Теперь совпадает с Kotlin
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test")) // Версия будет 1.9.0 через BOM

    // Ktor
    implementation("io.ktor:ktor-server-core:2.3.11")
    implementation("io.ktor:ktor-server-netty:2.3.11")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.11")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.11")
    implementation("io.ktor:ktor-server-auth:2.3.11")
    implementation("io.ktor:ktor-server-auth-jwt:2.3.11")

    // Security
    implementation("at.favre.lib:bcrypt:0.10.2")

    // Database
    implementation("org.jetbrains.exposed:exposed-core:0.44.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.44.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.44.0")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.postgresql:postgresql:42.7.3")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.4.14")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Testing
    testImplementation("io.ktor:ktor-server-test-host:2.3.11")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17) // Исправлено: Java 17 (LTS версия)
}