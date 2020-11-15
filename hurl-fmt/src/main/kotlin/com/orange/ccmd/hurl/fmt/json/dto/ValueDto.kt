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

package com.orange.ccmd.hurl.fmt.json.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Encoder

sealed class ValueDto {

    @ExperimentalSerializationApi
    @Serializer(forClass = ValueDto::class)
    companion object : KSerializer<ValueDto> {
        override fun serialize(encoder: Encoder, value: ValueDto) {
            when (value) {
                is BooleanValueDto -> encoder.encodeBoolean(value.value)
                is StringValueDto -> encoder.encodeString(value.value)
                NullValueDto -> encoder.encodeNull()
                is DoubleValueDto -> encoder.encodeDouble(value.value)
                is LongValueDto -> encoder.encodeLong(value.value)
            }
        }
    }
}

data class BooleanValueDto(
    val value: Boolean
) : ValueDto()

data class StringValueDto(
    val value: String
) : ValueDto()

object NullValueDto : ValueDto()

data class DoubleValueDto(
    val value: Double
) : ValueDto()

data class LongValueDto(
    val value: Long
) : ValueDto()

