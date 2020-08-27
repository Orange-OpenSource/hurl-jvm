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
    jacoco
}

val jacksonVersion = "2.11.2"
val jsoupVersion = "1.12.2"
val jsonPathVersion = "2.4.0"
val apacheHttpVersion = "4.5.11"
val sl4jVersion = "1.7.30"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("org.jsoup:jsoup:$jsoupVersion")
    implementation("com.jayway.jsonpath:json-path:$jsonPathVersion")
    implementation("org.apache.httpcomponents:httpclient:$apacheHttpVersion")
    implementation("org.apache.httpcomponents:httpmime:$apacheHttpVersion")
    implementation("org.slf4j:slf4j-api:$sl4jVersion")

    testImplementation("org.slf4j:slf4j-nop:$sl4jVersion")
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = false
        csv.isEnabled = true
    }
}

