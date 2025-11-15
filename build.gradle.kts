plugins {
    id("build.version")
    id("build.java")
    id("build.test")
    id("build.coverage")
    id("build.publishing")
}

dependencies {
    api(libs.slf4j.api)
    api(libs.jetbrains.annotations)
    api(libs.quark.eventbus)
    testImplementation(libs.logback.classic)
    testImplementation(libs.spock.core)
    testImplementation(libs.jsonassert)
    testImplementation(libs.junit.platform)
}
