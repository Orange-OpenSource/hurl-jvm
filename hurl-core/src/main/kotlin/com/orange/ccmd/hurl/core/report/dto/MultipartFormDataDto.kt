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

import com.orange.ccmd.hurl.core.http.FileFormData
import com.orange.ccmd.hurl.core.http.FormData
import com.orange.ccmd.hurl.core.http.TextFormData
import kotlinx.serialization.Serializable

@Serializable
data class MultipartFormDataDto(
    val textDatas: List<KeyValueDto>,
    val fileDatas: List<FileDataDto>,
)

fun List<FormData>.toMultipartFormDataDto(): MultipartFormDataDto {
    return MultipartFormDataDto(
        textDatas = filterIsInstance<TextFormData>().map { KeyValueDto(it.name, it.value) },
        fileDatas = filterIsInstance<FileFormData>().map {
            FileDataDto(
                name = it.name,
                filename = it.fileName,
                contentType = it.contentType
            )
        }
    )
}