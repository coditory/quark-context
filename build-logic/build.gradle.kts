plugins {
    `kotlin-dsl`
    kotlin("jvm") version embeddedKotlinVersion
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.kotlin.gradle)
    implementation(libs.gradle.nexus.publish)
    compileOnly(files(libs::class.java.protectionDomain.codeSource.location))
}
