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

package com.orange.ccmd.hurl.core.codec

import com.orange.ccmd.hurl.core.utils.byteArray
import com.orange.ccmd.hurl.core.utils.string
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class GzipDecoderTest {

    @Test
    fun `decode a gzip compressed byte stream`() {

        val coded = listOf(
            0x1f, 0x8b, 0x08, 0x08, 0x03, 0x38, 0x88, 0x5f, 0x02, 0xff, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x2e,
            0x62, 0x69, 0x6e, 0x00, 0xf3, 0x48, 0xcd, 0xc9, 0xc9, 0x57, 0x28, 0xcf, 0x2f, 0xca, 0x49, 0x51,
            0x04, 0x00, 0x95, 0x19, 0x85, 0x1b, 0x0c, 0x00, 0x00, 0x00,
        ).byteArray()

        val decoded = GzipDecoder.decode(bytes = coded)
        assertEquals("Hello world!", decoded.string())
    }
}