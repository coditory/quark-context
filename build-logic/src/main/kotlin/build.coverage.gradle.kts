plugins {
    id("java-library")
    id("groovy")
    id("jacoco")
}

val libs = extensions.getByType(org.gradle.accessors.dm.LibrariesForLibs::class)

// coverage introduces time overhead
// enable coverage on demand only
// ./gradlew ... -Pcoverage
// ...or with a task
// ./gradlew ... coverage
val coverageEnabled = (project.hasProperty("coverage") && project.properties["coverage"] != "false") ||
    project.gradle.startParameter.taskNames.contains("coverage")

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

tasks.withType<Test> {
    configure<JacocoTaskExtension> {
        isEnabled = coverageEnabled
    }
}

tasks.register<JacocoReport>("coverage") {
    description = "Creates combined coverage report"
    executionData(fileTree(project.buildDir).include("jacoco/*.exec"))
    sourceSets(project.extensions.getByType(JavaPluginExtension::class).sourceSets.getByName("main"))
    dependsOn(tasks.findByName("test"), tasks.findByName("integrationTest"))
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}