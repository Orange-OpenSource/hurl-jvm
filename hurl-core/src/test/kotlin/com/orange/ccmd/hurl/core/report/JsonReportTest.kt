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

import com.orange.ccmd.hurl.core.run.RunResult
import org.junit.jupiter.api.Test
import java.io.File
import java.time.Duration
import kotlin.test.assertEquals

class JsonReportTest {

    @Test
    fun `export a run result`() {
        val runResult = RunResult(
            duration = Duration.ofSeconds(12000),
            entryResults = emptyList(),
        )

        assertEquals("""{"entries":[],"success":true,"duration":12000000}""", JsonReport(runResult).build())
    }
}