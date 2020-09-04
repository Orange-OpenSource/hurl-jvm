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