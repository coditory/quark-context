import java.time.Duration

plugins {
    `java-library`
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin")
}

// creating publishable jar introduces time overhead
// add "publish" property to enable signing and javadoc and sources in the jar
// ./gradlew ... -Ppublish
// ...or with a task
// ./gradlew ... coverage
val publishEnabled = (project.hasProperty("publish") && project.properties["publish"] != "false") ||
    project.gradle.startParameter.taskNames.contains("publishToSonatype") ||
    project.gradle.startParameter.taskNames.contains("publishToMavenLocal")

java {
    if (publishEnabled) {
        withSourcesJar()
        withJavadocJar()
    }
}

group = "com.coditory.quark"
description = "Coditory Quark Context - dependency injection context"

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            from(components["java"])

            pom {
                name.set(project.name)
                description.set(project.description)
                url.set("https://github.com/coditory/quark-i18n")
                organization {
                    name.set("Coditory")
                    url.set("https://coditory.com")
                }
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("coditory")
                        name.set("Coditory")
                        email.set("admin@coditory.com")
                    }
                }
                scm {
                    connection.set("scm:git@github.com:coditory/quark-i18n.git")
                    developerConnection.set("scm:git@github.com:coditory/quark-i18n.git")
                    url.set("https://github.com/coditory/quark-i18n")
                }
                issueManagement {
                    system.set("GitHub")
                    url.set("https://github.com/coditory/quark-i18n/issues")
                }
            }
        }
    }
}

if (publishEnabled && !project.gradle.startParameter.taskNames.contains("publishToMavenLocal")) {
    val signingKey: String? = System.getenv("SIGNING_KEY")
    val signingPwd: String? = System.getenv("SIGNING_PASSWORD")
    if (signingKey.isNullOrBlank() || signingPwd.isNullOrBlank()) {
        logger.info("Signing disabled as the GPG key was not found. Define SIGNING_KEY and SIGNING_PASSWORD to enable.")
    }
    signing {
        useInMemoryPgpKeys(signingKey, signingPwd)
        sign(publishing.publications["maven"])
    }
}

nexusPublishing {
    connectTimeout.set(Duration.ofMinutes(5))
    clientTimeout.set(Duration.ofMinutes(5))
    repositories {
        sonatype {
            System.getenv("NEXUS_USERNAME")?.let { username.set(it) }
            System.getenv("NEXUS_PASSWORD")?.let { password.set(it) }
        }
    }
}
