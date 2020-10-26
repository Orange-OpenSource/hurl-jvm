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

package com.orange.ccmd.hurl.core.run

import java.io.File

/**
 * Runner options.
 *
 * This class represents all options of an Hurl runner
 *
 * @property variables map of variable (pair of string for name and value), that are injected in the
 * construction of a runner, and latter augmented by captures during the execution
 * of a session.
 * @property fileRoot root directory for body file includes. Default is hurlFile directory.
 * @property outputHeaders include protocol headers in the output. Default is false.
 * @property verbose turn off/on verbosity on log message
 * @property allowsInsecure allow connections to SSL sites without certs. Default is false.
 * @property proxy use proxy on given port (ex: localhost:3128)
 * @property followsRedirect follow redirect (HTTP 3xx status code). Default is false.
 * @property compressed request a compressed response using one of the algorithms br, gzip, deflate and automatically decompress the content.
 * @property user Specify the user name and password to use for server authentication.
 */
data class Options(
    val variables: Map<String, String> = emptyMap(),
    val fileRoot: File,
    val outputHeaders: Boolean = false,
    val verbose: Boolean = false,
    val allowsInsecure: Boolean = false,
    val proxy: String? = null,
    val followsRedirect: Boolean = false,
    val toEntry: Int? = null,
    val compressed: Boolean = false,
    val user: String? = null,
)