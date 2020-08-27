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

package com.orange.ccmd.hurl.core.utils

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonToken

/**
 * Returns the byte number of the valid JSON data in the [buffer].
 *
 * If there is no valid JSON at the beginning of the [buffer], returns null.
 */
internal fun getJsonByteCount(buffer: ByteArray): Int? {
    val parser = JsonFactory().createParser(buffer)
    val token = try {
        parser.nextToken()
    } catch (e: Exception) {
        parser.close()
        return null
    }
    val total = when (token) {
        JsonToken.START_OBJECT, JsonToken.START_ARRAY -> {
            try {
                parser.skipChildren()
            } catch (e: Exception) {
                parser.close()
                return null
            }
            parser.tokenLocation.byteOffset.toInt() + 1
        }
        JsonToken.VALUE_STRING -> parser.text.toByteArray().size + 2
        JsonToken.VALUE_TRUE, JsonToken.VALUE_FALSE,
        JsonToken.VALUE_NUMBER_FLOAT, JsonToken.VALUE_NUMBER_INT,
        JsonToken.VALUE_NULL -> parser.text.toByteArray().size
        else -> {
            parser.close()
            return null
        }
    }
    parser.close()
    return total
}

internal fun parseJsonString(text: String): String? {
    val parser = JsonFactory().createParser(text)
    val token = try {
        parser.nextToken()
    } catch (e: Exception) {
        parser.close()
        return null
    }
    val value = if (token == JsonToken.VALUE_STRING) {
        parser.text
    } else {
        null
    }
    parser.close()
    return value
}