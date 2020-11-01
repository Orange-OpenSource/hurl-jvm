package com.orange.ccmd.hurl.core.report.dto

import com.orange.ccmd.hurl.core.run.EntryResult
import kotlinx.serialization.Serializable

@Serializable
data class EntryResultDto(
    val requestSpec: RequestSpecDto?,
    val response: ResponseDto?
)

fun EntryResult.toEntryResultDto(): EntryResultDto {
    return EntryResultDto(
        requestSpec = httpRequestSpec?.toRequestDto(),
        response = httpResponse?.toResponseDto()
    )
}