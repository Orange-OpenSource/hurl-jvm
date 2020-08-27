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

package com.orange.ccmd.hurl.core.query.jsonpath

sealed class JsonType

data class JsonNumber(val value: Number) : JsonType()

data class JsonString(val value: String) : JsonType()

data class JsonBoolean(val value: Boolean): JsonType()

data class JsonArray(val value: List<JsonType>): JsonType()

data class JsonObject(val value: Map<String, JsonType>) : JsonType()

class JsonNull: JsonType() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    override fun toString(): String {
        return "JsonNull()"
    }

}

fun toJson(value: Any?): JsonType {
    return when (value) {
        null -> JsonNull()
        is String -> JsonString(value)
        is Number -> JsonNumber(value)
        is Boolean -> JsonBoolean(value)
        is List<*> -> JsonArray(value.map { toJson(it) })
        is Map<*,*> -> {
            val obj = mutableMapOf<String, JsonType>()
            for( (k, v) in value) {
                if (k !is String) {
                    throw IllegalArgumentException()
                }
                obj[k] = toJson(v)
            }
            JsonObject(obj)
        }
        else -> throw IllegalArgumentException()
    }
}