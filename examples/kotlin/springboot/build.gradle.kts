plugins {
    kotlin("jvm") version "2.4.10"
    kotlin("plugin.spring") version "2.4.10"
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "edu.apu.pssdk.example"
version = "1.0.0"

repositories {
    mavenCentral()
}

// Must match the PeopleTools release of your App Server; grab the jar from there.
val psjoaVersion = System.getenv("PSJOA_JAR_VERSION") ?: "8.62.07"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.0")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("edu.apu.pssdk:pssdk:4.0.0")
    // CI.toProxyObject() returns a Graal ProxyObject; pssdk keeps polyglot runtime-scoped.
    implementation("org.graalvm.polyglot:polyglot:25.0.1")
    implementation("com.oracle.peoplesoft:psjoa:$psjoaVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

tasks.bootJar {
    archiveFileName.set("pssdk-example-kotlin.jar")
}

tasks.test {
    useJUnitPlatform()
}
