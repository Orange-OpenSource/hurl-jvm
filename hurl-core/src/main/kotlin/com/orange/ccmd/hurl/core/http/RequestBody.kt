package com.orange.ccmd.hurl.core.http

sealed class RequestBody(val data: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RequestBody

        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        return data.contentHashCode()
    }
}

class BinaryRequestBody(data: ByteArray): RequestBody(data)

class JsonRequestBody(data: ByteArray): RequestBody(data)

class XmlRequestBody(data: ByteArray): RequestBody(data)
