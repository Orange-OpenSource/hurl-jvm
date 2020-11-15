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

package com.orange.ccmd.hurl.fmt.json.dto

import com.orange.ccmd.hurl.core.ast.Base64
import com.orange.ccmd.hurl.core.ast.Bytes
import com.orange.ccmd.hurl.core.ast.File
import com.orange.ccmd.hurl.core.ast.Json as HurlJson
import com.orange.ccmd.hurl.core.ast.RawString
import com.orange.ccmd.hurl.core.ast.Xml
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

@Serializable
sealed class BytesDto

@Serializable
@SerialName("json")
data class JsonDto (
    val value: JsonElement
): BytesDto()

@Serializable
@SerialName("base64")
data class Base64Dto (
    val value: String
): BytesDto()

@Serializable
@SerialName("file")
data class FileDto (
    val filename: String
): BytesDto()

@Serializable
@SerialName("raw-string")
data class RawStringDto (
    val value: String
): BytesDto()

@Serializable
@SerialName("xml")
data class XmlDto (
    val value: String
): BytesDto()

fun Bytes.toBytesDto(): BytesDto {
    return when (this) {
        is HurlJson -> {
            val element = Json.parseToJsonElement(text)
            JsonDto(value = element)
        }
        is Base64 -> Base64Dto(value = base64String.text)
        is File -> FileDto(filename = fileName.value)
        is RawString -> RawStringDto(value = value)
        is Xml -> XmlDto(value = text)
    }
}