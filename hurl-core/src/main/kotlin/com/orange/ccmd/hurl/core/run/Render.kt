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

package com.orange.ccmd.hurl.core.run

import com.orange.ccmd.hurl.core.ast.Base64
import com.orange.ccmd.hurl.core.ast.Bytes
import com.orange.ccmd.hurl.core.ast.ContainPredicate
import com.orange.ccmd.hurl.core.ast.EqualExprPredicate
import com.orange.ccmd.hurl.core.ast.Cookie as CookieNode
import com.orange.ccmd.hurl.core.ast.EqualStringPredicate
import com.orange.ccmd.hurl.core.ast.FileParam
import com.orange.ccmd.hurl.core.ast.IncludeStringPredicate
import com.orange.ccmd.hurl.core.ast.Header as HeaderNode
import com.orange.ccmd.hurl.core.ast.Json
import com.orange.ccmd.hurl.core.ast.MatchPredicate
import com.orange.ccmd.hurl.core.ast.MultipartFormDataSection
import com.orange.ccmd.hurl.core.ast.Param
import com.orange.ccmd.hurl.core.ast.RawString
import com.orange.ccmd.hurl.core.ast.StartWithPredicate
import com.orange.ccmd.hurl.core.ast.Url
import com.orange.ccmd.hurl.core.ast.Xml
import com.orange.ccmd.hurl.core.http.Cookie
import com.orange.ccmd.hurl.core.http.FileFormData
import com.orange.ccmd.hurl.core.http.FormData
import com.orange.ccmd.hurl.core.http.FormParam
import com.orange.ccmd.hurl.core.http.Header
import com.orange.ccmd.hurl.core.http.QueryStringParam
import com.orange.ccmd.hurl.core.http.TextFormData
import com.orange.ccmd.hurl.core.variable.VariableJar
import com.orange.ccmd.hurl.core.template.Template
import com.orange.ccmd.hurl.core.variable.Var
import java.io.File

/**
 * Renders a {@link Bytes} node to a ByteArray, using [variables] and file [fileRoot].
 * Only Json, RawString and Xml nodes can be templated. File is the only Byte node
 * that will be rendered using an os file.
 * @param variables variables to use in templates
 * @param fileRoot root directory for File body node
 * @return a byte array representing the Bytes node
 */
internal fun Bytes.toByteArray(variables: VariableJar, fileRoot: File): ByteArray {
    return when (this) {
        is Json -> {
            val rendered = Template.render(text, variables = variables, position = begin)
            rendered.toByteArray()
        }
        is Base64 -> base64String.value
        is com.orange.ccmd.hurl.core.ast.File -> {
            fileRoot.resolve(relative = fileName.value).readBytes()
        }
        is RawString -> {
            val rendered = Template.render(value, variables = variables, position = begin)
            rendered.toByteArray()
        }
        is Xml -> {
            val rendered = Template.render(text, variables = variables, position = begin)
            rendered.toByteArray()
        }
    }
}

/**
 * Renders a {@link Url} node to string, using [variables].
 * @param variables variables to use in templates
 * @return a string representing the Url node
 */
internal fun Url.toUrl(variables: VariableJar): String =
    Template.render(text = value, variables = variables, position = begin)

/**
 * Renders a {@link Header} node to a HTTP Header using [variables].
 * @param variables variables to use in templates
 * @return a HTTP Header
 */
internal fun HeaderNode.toHeader(variables: VariableJar): Header {
    val (name, rawValue) = toPair()
    val value = Template.render(text = rawValue, variables = variables, position = keyValue.value.begin)
    return Header(name = name, value = value)
}

/**
 * Renders a {@link Param} node to a HTTP QueryStringParam using [variables].
 * @param variables variables to use in templates
 * @return a HTTP query string param
 */
internal fun Param.toQueryStringParam(variables: VariableJar): QueryStringParam {
    val (name, rawValue) = toPair()
    val value = Template.render(text = rawValue, variables = variables, position = keyValue.value.begin)
    return QueryStringParam(name = name, value = value)
}

/**
 * Renders a {@link Param} node to a HTTP FormParam using [variables].
 * @param variables variables to use in templates
 * @return a HTTP form param
 */
internal fun Param.toFormParam(variables: VariableJar): FormParam {
    val (name, rawValue) = toPair()
    val value = Template.render(text = rawValue, variables = variables, position = keyValue.value.begin)
    return FormParam(name = name, value = value)
}

/**
 * Renders a {@link MultipartFormDataSection} to a list of form data, using [variables] and file [fileRoot].
 * @param variables variables to use in templates
 * @param fileRoot root directory for File body node
 * @return a list of {@link FormData} representing the list of form dats
 */
internal fun MultipartFormDataSection.toFormDatas(variables: VariableJar, fileRoot: File): List<FormData> {

    val formDatas = mutableListOf<FormData>()

    // First, map text form datas:
    val textFormDatas = params.map { it.toFormData(variables = variables) }
    formDatas.addAll(textFormDatas)

    // Then, map file form datas:
    val fileFormDatas = fileParams.map { it.toFormData(fileRoot = fileRoot) }
    formDatas.addAll(fileFormDatas)

    return formDatas
}

/**
 * Render a text form data to form data, using [variables]
 * @param variables variables to use in templates
 * @return a form data
 */
internal fun Param.toFormData(variables: VariableJar): FormData {
    val name = keyValue.key.value
    val rawValue = keyValue.value.value
    val value = Template.render(text = rawValue, variables = variables, position = keyValue.value.begin)
    return TextFormData(
        name = name,
        value = value
    )
}

/**
 * Render a file form data to form data, using [fileRoot]
 * @param fileRoot root directory for File body node
 * @return a form data
 */
internal fun FileParam.toFormData(fileRoot: File): FormData {
    val name = key.value
    val fileName = file.fileName.value
    val bytes = fileRoot.resolve(relative = fileName).readBytes()
    val contentType = file.contentType?.value
    return FileFormData(
        name = name,
        fileName = fileName,
        contentType = contentType,
        value = bytes
    )
}

/**
 * Renders a {@link Cookie} to a key-value string pair, using [variables].
 * @param variables variables to use in templates
 * @return a key-value string pair representing the Cookie nodes
 */
internal fun CookieNode.toCookie(variables: VariableJar): Cookie {
    val (name, rawValue) = toPair()
    val value = Template.render(text = rawValue, variables = variables, position = value.begin)
    return Cookie(
        domain = "",
        path = "/",
        secure = null,
        expires = null,
        name = name,
        value = value,
    )
}

/**
 * Renders a {@link EqualStringPredicate} value to a string using [variables].
 * @param variables variables to use in templates
 * @return a string representing the value of this predicate function.
 */
internal fun EqualStringPredicate.valueToString(variables: VariableJar): String = Template.render(text = expr.value, variables = variables, position = expr.begin)

internal fun EqualExprPredicate.value(variables: VariableJar): Var = Template.expr(name = expr.name.value, variables = variables, position = expr.begin)

/**
 * Renders a {@link IncludeStringPredicate} value to a string using [variables].
 * @param variables variables to use in templates
 * @return a string representing the value of this predicate function.
 */
internal fun IncludeStringPredicate.valueToString(variables: VariableJar): String = Template.render(text = expr.value, variables = variables, position = expr.begin)

/**
 * Renders a {@link StartWithPredicate} value to a string using [variables].
 * @param variables variables to use in templates
 * @return a string representing the value of this predicate function.
 */
internal fun StartWithPredicate.valueToString(variables: VariableJar): String = Template.render(text = expr.value, variables = variables, position = expr.begin)

/**
 * Renders a {@link ContainPredicate} value to a string using [variables].
 * @param variables variables to use in templates
 * @return a string representing the value of this predicate function.
 */
internal fun ContainPredicate.valueToString(variables: VariableJar): String = Template.render(text = expr.value, variables = variables, position = expr.begin)

/**
 * Renders a {@link MatchPredicate} value to a string using [variables].
 * @param variables variables to use in templates
 * @return a string representing the value of this predicate function.
 */
internal fun MatchPredicate.valueToString(variables: VariableJar): String = Template.render(text = expr.value, variables = variables, position = expr.begin)

