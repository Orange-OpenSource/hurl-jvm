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

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    jacoco
}

val brotliVersion = "0.1.2"
val jacksonVersion = "2.12.5"
val jsoupVersion = "1.13.1"
val jsonPathVersion = "2.6.0"
val apacheHttpVersion = "4.5.13"
val kotlinxSerializationJsonVersion = "1.2.2"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("org.jsoup:jsoup:$jsoupVersion")
    implementation("com.jayway.jsonpath:json-path:$jsonPathVersion")
    implementation("org.apache.httpcomponents:httpclient:$apacheHttpVersion")
    implementation("org.apache.httpcomponents:httpmime:$apacheHttpVersion")
    implementation("org.brotli:dec:$brotliVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationJsonVersion")
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = false
        csv.isEnabled = true
    }
}


publishing {
    publications {
        val maven = get("maven") as MavenPublication
        maven.pom {
            name.set("hurl-core")
            description.set("Core library for Hurl client.")
        }
    }
}




