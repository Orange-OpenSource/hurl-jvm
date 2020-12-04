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

import kotlinx.serialization.Serializable
import com.orange.ccmd.hurl.core.ast.Response

@Serializable
data class ResponseDto(
    val version: String? = null,
    val status: Int,
    val headers: List<KeyValueDto>? = null,
    val captures: List<CaptureDto>? = null,
    val asserts: List<AssertDto>? = null,
    val body: BytesDto? = null,
)

fun Response.toResponseDto(): ResponseDto {

    val asserts = assertsSection?.asserts?.map { it.toAssertDto() }
    val captures = capturesSection?.captures?.map { it.toCaptureDto() }

    return ResponseDto(
        version = if (version.value == "HTTP/*") {
            null
        } else {
            version.value
        },
        status = status.value,
        headers = if (headers.isEmpty()) {
            null
        } else {
            headers.map { KeyValueDto(name = it.name, value = it.value) }
        },
        asserts = if (asserts.isNullOrEmpty()) {
            null
        } else {
            asserts
        },
        captures = if (captures.isNullOrEmpty()) {
            null
        } else {
            captures
        },
        body = body?.bytes?.toBytesDto()
    )
}