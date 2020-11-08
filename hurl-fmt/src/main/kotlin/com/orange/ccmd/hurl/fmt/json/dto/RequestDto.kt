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
import com.orange.ccmd.hurl.core.ast.Request as RequestNode

@Serializable
data class RequestDto(
    val method: String,
    val url: String,
    val headers: List<KeyValueDto>? = null,
)

fun RequestNode.toRequest(): RequestDto {
    return RequestDto(
        method = method.value,
        url = url.value,
        headers = if (headers.isNotEmpty()) {
            headers.map { KeyValueDto(name = it.name, value = it.value) }
        } else {
            null
        }
    )
}