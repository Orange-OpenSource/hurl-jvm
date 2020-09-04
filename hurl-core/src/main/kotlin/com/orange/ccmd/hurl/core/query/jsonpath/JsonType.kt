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


/**
 * Represents a JSON object <https://en.wikipedia.org/wiki/JSON>.
 * JSON's basic data types are:
 * - [JsonNumber]: a signed decimal number that may contain a fractional part,
 * - [JsonString]: a sequence of zero or more Unicode characters,
 * - [JsonBoolean]: either of the values true or false,
 * - [JsonArray]: an ordered list of zero or more values, each of which may be of any type,
 * - [JsonObject]: a collection of name–value pairs where the names (also called keys) are strings,
 * - [JsonNull]: an empty value, using the word null
 * [toValue] converts a JsonType to standard Kotlin type (Number, Boolean etc..) and [toJson]
 * function convert a Kotlin Object to a JsonType if possible
 *
 */
sealed class JsonType {

    /**
     * Converts to a Kotlin object using standard types (Number, Boolean, null etc.)
     * @return a Kotlin object.
     */
    abstract fun toValue(): Any?

    companion object {

        /**
         * Converts a Kotlin object to JsonType if possible.
         * @return a [JsonType] representing the object.
         * @throws IllegalArgumentException if the conversion is not possible.
         */
        fun toJson(value: Any?): JsonType {
            return when (value) {
                null -> JsonNull
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
    }
}

data class JsonNumber(val value: Number) : JsonType() {
    override fun toValue(): Number = value
}

data class JsonString(val value: String) : JsonType()  {
    override fun toValue(): String = value
}

data class JsonBoolean(val value: Boolean) : JsonType()  {
    override fun toValue(): Boolean = value
}

data class JsonArray(val value: List<JsonType>) : JsonType()  {
    override fun toValue(): List<Any?> = value.map { it.toValue() }
}

data class JsonObject(val value: Map<String, JsonType>) : JsonType()  {
    override fun toValue(): Map<String, Any?> = value.mapValues { it.value.toValue() }
}

object JsonNull : JsonType()  {
    override fun toValue(): Any? = null
}

