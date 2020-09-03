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