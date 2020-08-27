package com.orange.ccmd.hurl.core.http

const val USER_AGENT = "User-Agent"
const val CONTENT_TYPE = "Content-Type"
const val CONTENT_LENGTH = "Content-Length"

data class Header(
    val name: String,
    val value: String
)