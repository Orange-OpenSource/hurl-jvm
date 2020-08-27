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