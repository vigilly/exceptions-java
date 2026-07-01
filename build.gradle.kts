plugins {
    `java-library`
    `maven-publish`
    signing
}

group = "dev.vigilly"
version = "0.1.0"
description = "Vigilly exceptions client for Java — a branded wrapper around the MIT-licensed sentry-java SDK."

repositories {
    mavenCentral()
}

val sentryVersion = "8.46.0"
val junitVersion = "5.11.3"

dependencies {
    // We wrap (depend on) the MIT-licensed Sentry Java SDK and expose a branded dev.vigilly API.
    api("io.sentry:sentry:$sentryVersion")

    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    // Compile to Java 17 bytecode using whatever JDK runs Gradle (>= 17).
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("Vigilly Exceptions Client for Java")
                description.set(project.description)
                url.set("https://github.com/vigilly/exceptions-java")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("vigilly")
                        name.set("Vigilly")
                        url.set("https://vigilly.dev")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/vigilly/exceptions-java.git")
                    developerConnection.set("scm:git:ssh://git@github.com/vigilly/exceptions-java.git")
                    url.set("https://github.com/vigilly/exceptions-java")
                }
            }
        }
    }
    repositories {
        maven {
            name = "sonatype"
            // Maven Central publishing via Sonatype. Credentials are supplied by the operator
            // at publish time (env vars or Gradle properties); the build itself never needs them.
            val releasesUrl = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
            val snapshotsUrl = uri("https://central.sonatype.com/repository/maven-snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsUrl else releasesUrl
            credentials {
                username = (findProperty("sonatypeUsername") as String?) ?: System.getenv("SONATYPE_USERNAME")
                password = (findProperty("sonatypePassword") as String?) ?: System.getenv("SONATYPE_PASSWORD")
            }
        }
    }
}

signing {
    // Only sign when a signing key is provided (operator-gated publishing). Keeps `build` green.
    val signingKey = (findProperty("signingKey") as String?) ?: System.getenv("SIGNING_KEY")
    val signingPassword = (findProperty("signingPassword") as String?) ?: System.getenv("SIGNING_PASSWORD")
    if (signingKey != null && signingKey.isNotBlank()) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["mavenJava"])
    }
}
