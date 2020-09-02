package com.orange.ccmd.hurl.core.report


/**
 * Type of reporter fo cli run sessions.
 * SIMPLE is the default reporter, outputing the response body of the last http call.
 * TEST is a unit test like reporter, with ansi color states (like RUNNING, SUCCEED, FAILED)
 */
enum class ReporterType(val type: Int) {
    SIMPLE(0),
    TEST(1),
}