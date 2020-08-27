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

import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

data class Proxy(val protocol: String = "http", val host: String, val port: Int = 1080) {

    companion object {

        fun fromString(text: String): Proxy {
            val reg = """(http:\/\/)?([a-zA-Z\d.]+)(:\d+)?""".toRegex()
            val matchResult = reg.matchEntire(text) ?: throw IllegalArgumentException("Invalid proxy string $text")

            var protocol = matchResult.groups[1]?.value ?: "http://"
            protocol = protocol.removeSuffix("://")
            val hostname = matchResult.groups[2]!!.value
            val portString = matchResult.groups[3]?.value ?: ":1080"
            val port = portString.removePrefix(":").toInt()
            return Proxy(protocol = protocol, host = hostname, port = port)
        }
    }
}