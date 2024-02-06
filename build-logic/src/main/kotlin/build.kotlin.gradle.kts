import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("build.java")
    kotlin("jvm")
}

val libs = extensions.getByType(org.gradle.accessors.dm.LibrariesForLibs::class)

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        allWarningsAsErrors = true
    }
}
