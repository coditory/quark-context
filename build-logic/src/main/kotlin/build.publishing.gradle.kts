import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.archivesName

plugins {
    `java-library`
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin")
}

group = "com.coditory.quark"
description = "Quark internationalization library"

publishing {
    publications.create<MavenPublication>("jvm") {
        artifactId = project.archivesName.get()
        from(components["java"])
        versionMapping {
            usage("java-api") {
                fromResolutionOf("runtimeClasspath")
            }
            usage("java-runtime") {
                fromResolutionResult()
            }
        }
        pom {
            name.set(project.archivesName.get())
            description.set(project.description ?: rootProject.description)
            url.set("https://github.com/coditory/quark-i18n")
            organization {
                name = "Coditory"
                url = "https://coditory.com"
            }
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("ogesaku")
                    name.set("ogesaku")
                    email.set("ogesaku@gmail.com")
                }
            }
            scm {
                connection.set("scm:git:git://github.com/coditory/quark-i18n.git")
                url.set("https://github.com/coditory/quark-i18n")
            }
            issueManagement {
                system.set("GitHub")
                url.set("https://github.com/coditory/quark-i18n/issues")
            }
        }
    }
}

signing {
    if (System.getenv("SIGNING_KEY")?.isNotBlank() == true && System.getenv("SIGNING_PASSWORD")?.isNotBlank() == true) {
        useInMemoryPgpKeys(System.getenv("SIGNING_KEY"), System.getenv("SIGNING_PASSWORD"))
    }
    sign(publishing.publications["jvm"])
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}

nexusPublishing {
    repositories {
        sonatype {
            System.getenv("OSSRH_STAGING_PROFILE_ID")?.let { stagingProfileId = it }
            System.getenv("OSSRH_USERNAME")?.let { username.set(it) }
            System.getenv("OSSRH_PASSWORD")?.let { password.set(it) }
        }
    }
}
