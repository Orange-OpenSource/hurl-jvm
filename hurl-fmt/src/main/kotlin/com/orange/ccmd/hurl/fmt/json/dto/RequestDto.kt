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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.orange.ccmd.hurl.core.ast.Request as RequestNode

@Serializable
data class RequestDto(
    val method: String,
    val url: String,
    val headers: List<KeyValueDto>? = null,
    @SerialName("query_string_params") val queryStringParams: List<KeyValueDto>? = null,
    @SerialName("form_params") val formParams: List<KeyValueDto>? = null,
    @SerialName("multipart_form_data") val multipartFormData: List<FormDataDto>? = null,
    val cookies: List<KeyValueDto>? = null,
    val body: BytesDto? = null
)

fun RequestNode.toRequest(): RequestDto {
    return RequestDto(
        method = method.value,
        url = url.value,
        headers = headersDto(),
        queryStringParams = queryStringParamsDto(),
        formParams = formParamsDto(),
        multipartFormData = multipartFormDatasDto(),
        cookies = cookiesDto(),
        body = body?.bytes?.toBytesDto()
    )
}

private fun RequestNode.headersDto(): List<KeyValueDto>? {
    return headers
        .ifEmpty { null }
        ?.map { KeyValueDto(name = it.name, value = it.value) }
}

private fun RequestNode.queryStringParamsDto(): List<KeyValueDto>? {
    return queryStringParamsSection
        ?.params
        ?.ifEmpty { null }
        ?.map { KeyValueDto(name = it.name, value = it.value) }
}

private fun RequestNode.formParamsDto(): List<KeyValueDto>? {
    return formParamsSection
        ?.params
        ?.ifEmpty { null }
        ?.map { KeyValueDto(name = it.name, value = it.value) }
}

private fun RequestNode.cookiesDto(): List<KeyValueDto>? {
    return cookiesSection
        ?.cookies
        ?.ifEmpty { null }
        ?.map { KeyValueDto(name = it.name.value, value = it.value.value) }
}

private fun RequestNode.multipartFormDatasDto(): List<FormDataDto>? {
    val section = multipartFormDataSection ?: return null
    val formDatas = mutableListOf<FormDataDto>()

    // First, map text form datas:
    val textFormDatas = section.params.map { it.toFormDataDto() }
    formDatas.addAll(textFormDatas)

    // Then, map file form datas:
    val fileFormDatas = section.fileParams.map { it.toFormDataDto() }
    formDatas.addAll(fileFormDatas)
    return formDatas.toList()
}