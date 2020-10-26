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


class BrotliDecoderTest {

    @Test
    fun `decode a brotli compressed byte stream`() {

        val coded = listOf(
           0x8f, 0x05, 0x80, 0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x64, 0x21, 0x03,
        ).byteArray()

        val decoded = BrotliDecoder.decode(bytes = coded)
        assertEquals("Hello world!", decoded.string())
    }
}