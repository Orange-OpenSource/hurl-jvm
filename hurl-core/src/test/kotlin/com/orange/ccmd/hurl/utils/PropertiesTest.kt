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

package com.orange.ccmd.hurl.utils

import com.orange.ccmd.hurl.core.utils.Properties
import org.junit.jupiter.api.Test
import kotlin.test.*

internal class TemplateTest {

    @Test
    fun `load a propery file with success`() {
        val props = Properties("test.properties").get
        assertEquals(mapOf("fruit" to "apple", "color" to "red", "comment" to "tasty"), props)
    }
}