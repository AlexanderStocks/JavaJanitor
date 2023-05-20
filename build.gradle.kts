import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.20-Beta"

}

group = "me.alex"
version = "1.0-SNAPSHOT"
val ktor_version: String by project

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:2.3.0")
    implementation("io.ktor:ktor-server-netty-jvm:2.3.0")
    implementation("io.ktor:ktor-client-core-jvm:2.3.0")
    implementation("io.ktor:ktor-client-apache-jvm:2.3.0")
    implementation("io.ktor:ktor-client-json-jvm:2.3.0")
    implementation("io.ktor:ktor-client-logging-jvm:2.3.0")
    implementation("io.ktor:ktor-client-auth-jvm:2.3.0")
    implementation("io.ktor:ktor-client-cio-jvm:2.3.0")
    implementation("io.ktor:ktor-client-logging-jvm:2.3.0")
    implementation("io.ktor:ktor-serialization-gson:2.3.0")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    implementation("fr.inria.gforge.spoon:spoon-core:10.3.0")
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.4.0.202211300538-r")
    implementation("com.auth0:java-jwt:4.3.0")
    implementation("org.kohsuke:github-api:1.313")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.wso2.orbit.org.yaml:snakeyaml:1.33.0.wso2v1")
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")
    implementation("org.bouncycastle:bcpkix-jdk15on:1.70")
    implementation("com.jcabi:jcabi-github:1.4.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("com.github.javaparser:javaparser-symbol-solver-core:3.25.2")
    implementation("org.jgrapht:jgrapht:1.5.2")
    implementation("org.jgrapht:jgrapht-core:1.5.2")
    implementation("org.netbeans.external:gradle-tooling-api:RELEASE170")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.11")
    implementation("com.google.googlejavaformat:google-java-format:1.17.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
}
tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_17.toString()
    targetCompatibility = JavaVersion.VERSION_17.toString()
}