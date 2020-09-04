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

import java.io.File

/**
 * Helper class for MIME type.
 */
class Mime {
    companion object {

        /**
         * Returns the content type for a list of know extension, given ea [fileName].
         * @param fileName input filename
         * @return a content type string if knows, null otherswise.
         */
        fun getContentType(fileName: String): String? {
            val ext = File(fileName).extension
            val contentTypes = mapOf(
                "gif" to "image/gif",
                "jpg" to "image/jpeg",
                "jpeg" to "image/jpeg",
                "png" to "image/png",
                "svg" to "image/svg+xml",
                "txt" to "text/plain",
                "htm" to "text/html",
                "html" to "text/html",
                "pdf" to "application/pdf",
                "xml" to "application/xml",
            )
            return contentTypes[ext]
        }

    }
}