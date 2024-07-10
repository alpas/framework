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
    implementation(project(":framework"))

    implementation("org.junit.jupiter:junit-jupiter:5.10.2")
    implementation("io.mockk:mockk:1.13.11")
    implementation("io.rest-assured:rest-assured:5.5.0")
    implementation("io.rest-assured:kotlin-extensions:5.5.0")
}