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

/**
 * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Encoding
 *
 * The Content-Encoding entity header is used to compress the media-type. When
 * present, its value indicates which encodings were applied to the entity-body.
 * It lets the client know how to decode in order to obtain the media-type referenced by the Content-Type header.
 */
enum class Encoding(val value: String) {
    GZIP("gzip"),
    COMPRESS("compress"),
    DEFLATE("deflate"),
    IDENTITY("identity"),
    BR("br");

    companion object {
        fun fromValue(value: String): Encoding? {
            return values().firstOrNull { it.value == value }
        }
    }
}
