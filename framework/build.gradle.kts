@file:Suppress("VulnerableLibrariesLocal")

val ktormVersion: String by project

plugins {
    kotlin("jvm")
}

repositories {
    maven("https://jitpack.io")
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    compileOnly("jakarta.servlet:jakarta.servlet-api:6.1.0")
    implementation("org.eclipse.jetty:jetty-webapp:11.0.21")
    implementation("org.eclipse.jetty:jetty-servlet:11.0.21")
    implementation("org.eclipse.jetty:jetty-server:11.0.21")
    implementation("com.github.danielnorberg:rut:v1.0")
    implementation("org.picocontainer:picocontainer:2.15")
    implementation("io.github.cdimascio:java-dotenv:5.1.3")
    implementation("io.github.classgraph:classgraph:4.8.65")
    implementation("com.github.simbiose:Encryption:2.0.1")
    implementation("org.slf4j:slf4j-api:1.8.0-beta4")
    implementation("de.mkammerer:argon2-jvm:2.6")
    implementation("uy.kohesive.klutter:klutter-core:2.6.0")
    implementation("org.apache.qpid:qpid-jms-client:0.48.0")
    implementation("org.simplejavamail:simple-java-mail:6.0.3")
    implementation("com.github.alpas:url-signer:0.1.7")
    implementation("com.github.freva:ascii-table:1.1.0")

    implementation("com.zaxxer:HikariCP:3.4.2")
    implementation("org.atteo:evo-inflector:1.2.2")
    implementation("io.pebbletemplates:pebble:3.1.3")
    implementation("com.github.ajalt:clikt:2.5.0")
    implementation("com.github.ajalt:mordant:1.2.1")
    implementation("io.github.microutils:kotlin-logging:1.7.8")
    implementation("me.liuwj.ktorm:ktorm-core:${ktormVersion}")
    implementation("me.liuwj.ktorm:ktorm-jackson:${ktormVersion}")
    implementation("me.liuwj.ktorm:ktorm-support-mysql:${ktormVersion}")
    implementation("me.liuwj.ktorm:ktorm-support-postgresql:${ktormVersion}")
    implementation("me.liuwj.ktorm:ktorm-support-sqlite:${ktormVersion}")
    implementation("com.github.marlonlom:timeago:4.0.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.+")
    implementation("com.github.javafaker:javafaker:1.0.2")
    listOf("fuel", "fuel-coroutines", "fuel-jackson").forEach { pck ->
        implementation("com.github.kittinunf.fuel:${pck}:2.3.1")
    }
    
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.0")
    testImplementation("io.mockk:mockk:1.9.3")
}
