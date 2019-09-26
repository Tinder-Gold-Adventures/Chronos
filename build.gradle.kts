import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "2.1.8.RELEASE"
    id("io.spring.dependency-management") version "1.0.8.RELEASE"
    kotlin("jvm") version "1.2.71"
    kotlin("plugin.spring") version "1.2.71"
}

group = "tinder.gold.adventures"
version = "0.0.1-SNAPSHOT"
java.apply {
	sourceCompatibility = JavaVersion.VERSION_1_8
	targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(spring("spring-boot-starter"))
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(spring("spring-boot-starter-test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
	testImplementation(kotlin("test"))
	testImplementation(kotlin("test-junit"))
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