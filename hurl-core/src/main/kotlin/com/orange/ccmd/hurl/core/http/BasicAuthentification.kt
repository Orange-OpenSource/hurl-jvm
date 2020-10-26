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

package com.orange.ccmd.hurl.core.http

import com.orange.ccmd.hurl.core.utils.string
import java.nio.charset.StandardCharsets
import java.util.*


data class BasicAuthentification(val user: String, val password: String) {

    companion object {
        fun fromString(param: String): BasicAuthentification {
            val tokens = param.split(":", limit = 2)
            if (tokens.isEmpty()) {
                throw IllegalArgumentException("param should be <user:password>")
            }
            return BasicAuthentification(user = tokens[0], password = tokens[1])
        }
    }

    val headerValue: String
    get() {
        val auth = "$user:$password"
        val encodedAuth = Base64.getEncoder().encode(auth.toByteArray(StandardCharsets.ISO_8859_1))
        return "Basic ${encodedAuth.string()}"
    }
}