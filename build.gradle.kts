/*
 * Copyright (C) 2020 Orange
 *
 * Hurl JVM (JVM Runner for https://hurl.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.30" apply false
    kotlin("plugin.serialization") version "1.5.30" apply false
    id("org.jetbrains.dokka") version "1.5.0"
    // Ajoute la task dependencyUpdates pour gérer les dépendances.
    id("com.github.ben-manes.versions") version "0.38.0"
    `maven-publish`
    signing
}

val jUnitVersion = "5.7.1"

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin= "org.jetbrains.dokka")

    repositories {
        mavenCentral()
    }

    dependencies {
        "testImplementation"(kotlin("test"))
        "testImplementation"("org.junit.jupiter:junit-jupiter-engine:$jUnitVersion")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    tasks.named<Test>("test") {
        useJUnitPlatform()
    }

    tasks.register<Jar>("sourcesJar") {
        from(project.the<SourceSetContainer>()["main"].allSource)
        archiveClassifier.set("sources")
    }

    tasks.register<Jar>("javadocJar") {
        archiveClassifier.set("javadoc")
        dependsOn("dokkaJavadoc")
    }

    publishing {

        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                artifact(tasks["sourcesJar"])
                artifact(tasks["javadocJar"])
            }
            .pom {
                packaging = "jar"
                url.set("https://hurl.dev")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                scm {
                    url.set("https://github.com/Orange-OpenSource/hurl-jvm")
                    connection.set("scm:git:git://github.com/Orange-OpenSource/hurl-jvm.git")
                    developerConnection.set("scm:git:ssh://git@github.com/Orange-OpenSource/hurl-jvm.git")
                }
                developers {
                    developer {
                        name.set("Jean-Christophe Amiel")
                        email.set("jeanchristophe.amiel@orange.com")
                    }
                }
            }
        }
        repositories {
            maven {
                val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
                val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots/"
                url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
                credentials {
                    username = System.getenv("SONATYPE_USERNAME")
                    password = System.getenv("SONATYPE_PASSWORD")
                }
            }
        }
    }

    signing {
        val signingKeyId = System.getenv("GNUGP_SIGNING_KEYID")
        val signingKey = System.getenv("GNUGP_SIGNING_KEY")
        val signingPassword = System.getenv("GNUGP_SIGNING_PASSWORD")
        useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
        sign(publishing.publications["maven"])
    }

}


project(":hurl-cli") {
    dependencies {
        "implementation"(project(":hurl-core"))
    }
}

project(":hurl-fmt") {
    dependencies {
        "implementation"(project(":hurl-core"))
    }
}

tasks.dependencyUpdates {
    resolutionStrategy {
        componentSelection {
            all {
                val rejected = listOf("alpha", "beta", "b", "rc", "cr", "pr", "m", "preview")
                    .map { qualifier -> Regex("(?i).*[.-]$qualifier[.\\d-]*") }
                    .any { it.matches(candidate.version) }
                if (rejected) {
                    reject("Release candidate")
                }

                // We reject some libraries whose versioning schema has changed
                // and that are problematic for last version resolution
                // see <https://github.com/ben-manes/gradle-versions-plugin/issues/434>
                val blackList = listOf(
                    "commons-cli:commons-cli:20040117.000000"
                )
                val version = "${candidate.group}:${candidate.module}:${candidate.version}"
                if (blackList.any { it == version }) {
                    reject("Blacklisted $version")
                }
            }
        }
    }
    checkForGradleUpdate = true
    outputFormatter = "json"
    outputDir = "$projectDir/build/dependencyUpdates"
}


