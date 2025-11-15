plugins {
    `java-library`
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin")
}

group = "com.coditory.quark"
description = "Coditory Quark Context - Dependency Injection"

publishing {
    publications.create<MavenPublication>("jvm") {
        artifactId = project.name
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
            name.set(project.name)
            description.set(project.description)
            url.set("https://github.com/coditory/quark-context")
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
                    id.set("coditory")
                    name.set("Coditory")
                    email.set("admin@coditory.com")
                }
            }
            scm {
                connection.set("scm:git:git://github.com/coditory/quark-context.git")
                url.set("https://github.com/coditory/quark-context")
            }
            issueManagement {
                system.set("GitHub")
                url.set("https://github.com/coditory/quark-context/issues")
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

nexusPublishing {
    repositories {
        sonatype {
            System.getenv("SONATYPE_STAGING_PROFILE_ID")?.let { stagingProfileId = it }
            System.getenv("SONATYPE_USERNAME")?.let { username.set(it) }
            System.getenv("SONATYPE_PASSWORD")?.let { password.set(it) }
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
        }
    }
}
