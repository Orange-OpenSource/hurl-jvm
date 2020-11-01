package com.orange.ccmd.hurl.core.report.dto
import com.orange.ccmd.hurl.core.run.RunResult
import kotlinx.serialization.Serializable

@Serializable
data class RunResultDto(
    val entries: List<EntryResultDto>,
    val success: Boolean,
    val duration: Int
)

fun RunResult.toRunResultDto(): RunResultDto {
    return RunResultDto(
        entries = entryResults.map { it.toEntryResultDto() },
        success = succeeded,
        duration = duration.toMillis().toInt()
    )
}