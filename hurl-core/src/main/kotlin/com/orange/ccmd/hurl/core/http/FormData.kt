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

package com.orange.ccmd.hurl.core.http

sealed class FormData

data class TextFormData(
    val name: String, val value: String
) : FormData()

data class FileFormData(
    val name: String, val fileName: String, val contentType: String?, val value: ByteArray
) : FormData() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileFormData

        if (name != other.name) return false
        if (fileName != other.fileName) return false
        if (contentType != other.contentType) return false
        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + (contentType?.hashCode() ?: 0)
        result = 31 * result + value.contentHashCode()
        return result
    }
}