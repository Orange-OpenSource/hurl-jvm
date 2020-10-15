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

class ZlibDecoderTest {

    @Test
    fun `decode a zlib compressed byte stream`() {

        val coded = listOf(
            0x78, 0x9c, 0xf3, 0x48, 0xcd, 0xc9, 0xc9, 0x2f, 0xcf, 0x2f, 0xca, 0x49, 0x51, 0x04, 0x00, 0x1a,
            0x34, 0x04, 0x3e
        ).byteArray()

        val decoded = ZlibDecoder.decode(bytes = coded)
        assertEquals("Helloworld!", decoded.string())
    }
}