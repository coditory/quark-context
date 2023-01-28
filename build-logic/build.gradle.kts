plugins {
    `kotlin-dsl`
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
