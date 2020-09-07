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

package com.orange.ccmd.hurl.fmt.lint

import com.orange.ccmd.hurl.core.ast.HurlFile

/*
internal fun HurlFile.lint(): String {
    var text = ""
    text += entries?.fold(text) { t, entry -> t + entry.lint() }
    return text
}

internal fun Assert.lint(): String {
    var text = ""
    text = text add comments?.lint()
    text = text add query.lint()
    text = text add spaces0.lint()
    text = text add predicate.lint()
    text = text add spaces1?.lint()
    text = text add comment?.lint()
    text = text add eol.value.trimTrailingSpaces()
    return text
}

internal fun Asserts.lint(): String {
    var text = ""
    text = text add comments?.lint()
    text = text add sectionHeader.lint()
    text = text add spaces?.lint()
    text = text add eol.value.trimTrailingSpaces()
    text = asserts.fold(text) { t, assert -> t + assert.lint() }
    return text
}

internal fun Base64Body.lint(): String {
    var text = ""
    text = text add prefix.lint()
    text = text add base64.lint()
    text = text add suffix.lint()
    return text
}

internal fun Base64String.lint(): String {
    return text
}

internal fun Body.lint(): String {
    var text = ""
    text = text add comments?.lint()
    text = text add json?.lint()
    text = text add multilineString?.lint()
    text = text add base64Body?.lint()
    text = text add spaces?.lint()
    text = text add comment?.lint()
    text = text add eol.value.trimTrailingSpaces()
    return text
}

internal fun Bool.lint(): String {
    return text
}

internal fun Capture.lint(): String {
    var text = ""

    // Remove trailing whitespace in comments.
    val cs = comments
    if (cs != null) {
        text = text add cs.rawText().trimTrailingSpaces()
    }
    text = text add key.lint()
    text = text add spaces0?.lint()
    text = text add colon.lint()
    text = text add spaces1?.lint()
    text = text add query.lint()
    text = text add spaces2?.lint()
    text = text add comment?.lint()
    text = text add eol.value.trimTrailingSpaces()
    return text
}

internal fun Captures.lint(): String {
    var text = ""
    text = text add comments?.lint()
    text = text add sectionHeader.lint()
    text = text add spaces?.lint()
    text = text add eol.value.trimTrailingSpaces()
    text = captures.fold(text) { t, capture -> t + capture.lint() }
    return text
}

internal fun Comment.lint(): String {
    return value
}

internal fun CommentLine.lint(): String {
    var text = ""
    text = text add comment.lint()
    text = text add eol.value.trimTrailingSpaces()
    text = text add whitespaces?.lint()
    return text
}

internal fun Comments.lint(): String {
    var text = ""
    text = comments.fold(text) { t, commentLine -> t + commentLine.lint() }
    return text
}

internal fun Cookie.lint(): String {
    var text = ""
    text = text add comments?.lint()
    text = text add key.lint()
    text = text add spaces0?.lint()
    text = text add colon.lint()
    text = text add spaces1?.lint()
    text = text add cookieValue.lint()
    text = text add spaces2?.lint()
    text = text add comment?.lint()
    text = text add eol.value.trimTrailingSpaces()
    return text
}

internal fun Cookies.lint(): String {
    var text = ""
    text = text add comments?.lint()
    text = text add sectionHeader.lint()
    text = text add spaces?.lint()
    text = text add eol.value.trimTrailingSpaces()
    text = cookies.fold(text) { t, cookie -> t + cookie.lint() }
    return text
}

internal fun CookieValue.lint(): String {
    return value
}

internal fun Entry.lint(): String {
    var text = ""
    text = text add request.lint()
    text = text add response?.lint()
    return text
}

internal fun Eol.lint(): String {
    return value
}

internal fun Float.lint(): String {
    return text
}

internal fun FormParams.lint(): String {
    var text = ""
    text = text add comments?.lint()
    text = text add sectionHeader.lint()
    text = text add spaces?.lint()
    text = text add eol.value.trimTrailingSpaces()
    text = params.fold(text) { t, param -> t + param.lint() }
    return text
}

internal fun Headers.lint(): String {
    var text = ""
    text = headers.fold(text) { t, header -> t + header.lint() }
    return text
}

internal fun Integer.lint(): String {
    return text
}

internal fun Json.lint(): String {
    return text
}

internal fun JsonString.lint(): String {
    return text
}

internal fun Key.lint(): String {
    var text = ""
    text = text add jsonString?.lint()
    text = text add keyString?.lint()
    return text
}

internal fun KeyString.lint(): String {
    return value
}

internal fun KeyValue.lint(): String {
    var text = ""
    text = text add comments?.lint()
    text = text add key.lint()
    text = text add spaces0?.lint()
    text = text add colon.lint()
    text = text add spaces1?.lint()
    text = text add value.lint()
    text = text add spaces2?.lint()
    text = text add eol.value.trimTrailingSpaces()
    return text
}

internal fun Literal.lint(): String {
    return value
}

internal fun Method.lint(): String {
    return value
}

internal fun MultilineString.lint(): String {
    return text
}

internal fun Natural.lint(): String {
    return text
}

internal fun Predicate.lint(): String {
    var text = ""
    text = text add type.lint()
    text = text add spaces.lint()
    text = text add predicateValue.lint()
    return text
}

internal fun PredicateType.lint(): String {
    return text
}

internal fun PredicateValue.lint(): String {
    var text = ""
    text = text add integer?.lint()
    text = text add float?.lint()
    text = text add bool?.lint()
    text = text add string?.lint()
    return text
}

internal fun QsParams.lint(): String {
    var text = ""
    text = text add comments?.lint()
    text = text add sectionHeader.lint()
    text = text add spaces?.lint()
    text = text add eol.value.trimTrailingSpaces()
    text = params.fold(text) { t, param -> t + param.lint() }
    return text
}

internal fun Query.lint(): String {
    var text = ""
    text = text add spaces0?.lint()
    text = text add type.lint()
    text = text add spaces1?.lint()
    text = text add expr?.lint()
    return text
}

internal fun QueryExpr.lint(): String {
    var text = ""
    text = text add jsonString?.lint()
    text = text add queryString?.lint()
    return text
}

internal fun QueryString.lint(): String {
    return value
}

internal fun QueryType.lint(): String {
    return text
}

internal fun Request.lint(): String {
    var text = ""
    text = text add comments?.lint()
    text = text add method.lint()
    text = text add spaces0.lint()
    text = text add url.lint()
    text = text add spaces1?.lint()
    text = text add comment?.lint()
    text = text add eol.value.trimTrailingSpaces()
    text = text add headers?.lint()
    text = text add cookies?.lint()
    text = text add qsParams?.lint()
    text = text add formParams?.lint()
    text = text add body?.lint()
    return text
}

internal fun Response.lint(): String {
    var text = ""
    text = text add comments?.lint()
    text = text add version.lint()
    text = text add spaces0.lint()
    text = text add status.lint()
    text = text add spaces1?.lint()
    text = text add comment?.lint()
    text = text add eol.value.trimTrailingSpaces()
    text = text add headers?.lint()
    text = text add captures?.lint()
    text = text add asserts?.lint()
    text = text add body?.lint()
    return text
}

internal fun SectionHeader.lint(): String {
    return value
}

internal fun Spaces.lint(): String {
    return value
}

internal fun Status.lint(): String {
    var text = ""
    text = text add natural.lint()
    return text
}

internal fun Url.lint(): String {
    return value
}

internal fun Value.lint(): String {
    var text = ""
    text = text add jsonString?.lint()
    text = text add valueString?.lint()
    return text

}

internal fun ValueString.lint(): String {
    return value
}

internal fun Version.lint(): String {
    return value
}

internal fun Whitespaces.lint(): String {
    return value
}

internal infix fun String.add(other: String?): String {
    return if (other != null) {
        this + other
    } else {
        this
    }
}

fun CommentLine.rawText(): String {
    var text = ""
    text = text add comment.value
    text = text add eol.value
    text = text add whitespaces?.value
    return text
}

fun Comments.rawText(): String {
    var text = ""
    text = comments.fold(text) { t, commentLine -> t + commentLine.rawText() }
    return text
}

internal fun String.trimTrailingSpaces(): String {
    return trimEnd { it == ' ' || it == '\t' }
}
 */
internal fun HurlFile.lint(): String {
    TODO()
}
