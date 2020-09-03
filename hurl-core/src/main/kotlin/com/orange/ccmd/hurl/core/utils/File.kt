package com.orange.ccmd.hurl.core.utils

import java.io.File

val File.extension: String
    get() = name.substringAfterLast('.', "")