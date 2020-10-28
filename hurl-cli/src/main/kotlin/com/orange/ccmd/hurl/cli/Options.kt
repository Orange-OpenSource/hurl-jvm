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

package com.orange.ccmd.hurl.cli

data class Options(
    val help: Boolean = false,
    val version: Boolean = false,
    val verbose: Boolean = false,
    val followRedirect: Boolean = false,
    val insecure: Boolean = false,
    val proxy: String? = null,
    val variables: Map<String, String> = emptyMap(),
    val fileRoot: String? = null,
    val include: Boolean = false,
    val toEntry: Int? = null,
    val compressed: Boolean = false,
    val outputFile: String? = null,
    val user: String? = null,
    val connectTimeoutInSecond: Int = 60,
    val maxTime: Int? = null
) {
    override fun toString(): String {
        var string = "* version: $version\n"
        string += "* verbose: $verbose\n"
        string += "* include: $include\n"
        string += "* variables:\n"
        variables.forEach { (k, v) -> string += "*   $k -> $v\n" }
        string += "* fileRoot: ${fileRoot ?: ""}\n"
        string += "* insecure: $insecure\n"
        string += "* proxy: ${proxy ?: ""}\n"
        string += "* toEntry: ${toEntry ?: ""}\n"
        string += "* compressed: $compressed\n"
        string += "* outputFile: ${outputFile ?: ""}\n"
        string += "* user: ${user ?: ""}\n"
        string += "* connectTimeout: $connectTimeoutInSecond\n"
        return string
    }
}