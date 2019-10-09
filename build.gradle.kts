import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.50"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.3.50"
    id("org.springframework.boot") version "2.1.8.RELEASE"
    id("io.spring.dependency-management") version "1.0.8.RELEASE"
    kotlin("plugin.spring") version "1.2.71"
}

group = "tinder.gold.adventures"
version = "0.0.1-SNAPSHOT"
java.apply {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
    maven(url = "https://repo.eclipse.org/content/repositories/paho-releases/")
    maven(url = "https://plugins.gradle.org/m2/")
}

dependencies {
    implementation(kotlin("allopen:1.3.50"))
    implementation(spring("spring-boot-starter"))
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")

    implementation("io.github.microutils:kotlin-logging:1.7.6")
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.0")

    testImplementation(spring("spring-boot-starter-test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
    testImplementation("io.mockk:mockk:1.9")
    testCompile("org.assertj:assertj-core:3.11.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

tasks.bootRun {
    main = "$group.ChronosApplication.kt"
    args("--spring.profiles.active=local")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

fun spring(module: String, version: String? = null): Any =
        quickDependency("org.springframework.boot", module, version)

fun quickDependency(group: String, module: String, version: String? = null): Any =
        "$group:$module${version?.let { ":$version" } ?: ""}"