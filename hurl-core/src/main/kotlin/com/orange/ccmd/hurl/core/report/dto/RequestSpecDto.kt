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

package com.orange.ccmd.hurl.core.report.dto

import com.orange.ccmd.hurl.core.http.HttpRequest
import kotlinx.serialization.Serializable
import java.nio.charset.CharacterCodingException

@Serializable
data class RequestSpecDto(
    val method: String,
    val url: String,
    val queryString: List<KeyValueDto>,
    val headers: List<KeyValueDto>,
    val cookies: List<KeyValueDto>,
    val form: List<KeyValueDto>,
    val multipartFormData: MultipartFormDataDto,
    val body: String?
)

fun HttpRequest.toRequestDto(): RequestSpecDto {
    return RequestSpecDto(
        method = method,
        url = url,
        queryString = queryStringParams.map { KeyValueDto(it.name, it.value) },
        headers = headers.map { KeyValueDto(it.name, it.value) },
        cookies = cookies.map { KeyValueDto(it.name, it.value) },
        form = formParams.map { KeyValueDto(it.name, it.value) },
        multipartFormData = multipartFormDatas.toMultipartFormDataDto(),
        body = try {
            text
        } catch (e: CharacterCodingException) {
            "<invalid text body>"
        }
    )
}
