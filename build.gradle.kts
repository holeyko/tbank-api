import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.2.20"
    application
}

group = "ru.holeyko"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.2.20")

    // Selenium WebDriver
    implementation("org.seleniumhq.selenium:selenium-java:4.35.0")
    implementation("org.seleniumhq.selenium:selenium-support:4.35.0")
    implementation("org.seleniumhq.selenium:selenium-chrome-driver:4.35.0")
    implementation("io.github.bonigarcia:webdrivermanager:6.3.2")
    
    // Logging
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("ch.qos.logback:logback-classic:1.5.6")
}

application {
    mainClass.set("ru.holeyko.tbankapi.MainKt")
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Need for gradle readLine
tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}
