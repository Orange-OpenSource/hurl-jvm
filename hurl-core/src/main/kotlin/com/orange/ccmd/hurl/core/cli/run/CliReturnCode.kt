package com.orange.ccmd.hurl.core.cli.run

enum class CliReturnCode(val value: Int) {
    SUCCESS(0),
    OPTIONS_PARSING_ERROR(1),
    INPUT_FILE_PARSING_ERROR(2),
    RUNTIME_ERROR(3),
    ASSERT_ERROR(4),
    UNKNOWN_ERROR(5)
}