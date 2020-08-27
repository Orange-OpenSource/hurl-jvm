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

import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets


class HttpResponse(
    val version: String,
    val code: Int,
    val headers: List<Pair<String, String>>,
    val charset: Charset,
    val mimeType: String,
    val body: ByteArray
) {

    val text: String?

    init {
        val decoder = charset.newDecoder()
        text = try {
            decoder.decode(ByteBuffer.wrap(body)).toString()
        } catch (ex: CharacterCodingException) {
            null
        }
    }

    override fun toString(): String {
        return "HttpResponse(version='$version', code=$code, headers=$headers)"
    }


}