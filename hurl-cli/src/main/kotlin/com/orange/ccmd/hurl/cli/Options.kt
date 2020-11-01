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

/**
 * Hurl Cli options.
 *
 * This class represents all options of the command line tool hurl
 *
 * @property help true show help message, false do nothing
 * @property version true show version of the cli tool, false do nothing
 * @property verbose Turn on verbose output on standard error stream Useful for debugging.
 * @property followRedirect follow redirect (HTTP 3xx status code). Default is false.
 * @property insecure explicitly allows Hurl to perform “insecure” SSL connections and transfers. (defualt false)
 * @property proxy use proxy on given port (ex: localhost:3128)
 * @property variables map of variable (pair of string for name and value), that are injected in the
 * construction of a runner, and latter augmented by captures during the execution
 * of a session.
 * @property fileRoot root directory for body file includes. Default is hurlFile directory.
 * @property include include protocol headers in the output. Default is false.
 * @property toEntry execute Hurl file to toEntry (starting at 1). Ignore the remaining of the file.
 * @property verbose turn off/on verbosity on log message
 * @property compressed request a compressed response using one of the algorithms br, gzip, deflate and automatically decompress the content.
 * @property user specify the user name and password to use for server authentication.
 * @property connectTimeoutInSecond timeout in seconds until a connection is established
 * @property maxTime maximum time in seconds that you allow a request/response to take. This is the standard timeout.
 */
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
)