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

package com.orange.ccmd.hurl.core.ast

import com.orange.ccmd.hurl.core.parser.Position

sealed class Node {
    abstract val begin: Position
    abstract val end: Position
}

/**
 * From https://hurl.dev/docs/grammar.html.
 */

data class Assert(
    override val begin: Position,
    override val end: Position,
    val lts: List<LineTerminator>,
    val spaces0: List<Space>,
    val query: Query,
    val spaces1: List<Space>,
    val predicate: Predicate,
    val lt: LineTerminator
) : Node()

data class AssertsSection(
    override val begin: Position,
    override val end: Position,
    val lts: List<LineTerminator>,
    val spaces: List<Space>,
    val header: SectionHeader,
    val lt: LineTerminator,
    val asserts: List<Assert>
) : ResponseSection()

data class Base64(
    override val begin: Position,
    override val end: Position,
    val prefix: Literal,
    val spaces0: List<Space>,
    val base64String: Base64String,
    val spaces1: List<Space>,
    val suffix: Literal
) : Bytes()

data class Body(
    override val begin: Position,
    override val end: Position,
    val lts: List<LineTerminator>,
    val spaces: List<Space>,
    val bytes: Bytes,
    val lt: LineTerminator
) : Node()

data class BodyQuery(
    override val begin: Position,
    override val end: Position,
    override val type: QueryType
) : Query()

data class Bool(override val begin: Position, override val end: Position, val value: Boolean, val text: String) : Node()

sealed class Bytes: Node()

data class Capture(
    override val begin: Position,
    override val end: Position,
    val lts: List<LineTerminator>,
    val spaces0: List<Space>,
    val name: HString,
    val spaces1: List<Space>,
    val colon: Literal,
    val spaces2: List<Space>,
    val query: Query,
    val spaces3: List<Space>,
    val subquery: Subquery?,
    val lt: LineTerminator
) : Node()

data class CapturesSection(
    override val begin: Position,
    override val end: Position,
    val lts: List<LineTerminator>,
    val spaces: List<Space>,
    val header: SectionHeader,
    val lt: LineTerminator,
    val captures: List<Capture>
) : ResponseSection()

data class Comment(override val begin: Position, override val end: Position, val value: String) : Node()

data class ContainPredicate(
    override val begin: Position,
    override val end: Position,
    override val type: PredicateType,
    val spaces: List<Space>,
    val expr: HString
) : PredicateFunc()

data class Cookie(
    override val begin: Position,
    override val end: Position,
    val lts: List<LineTerminator>,
    val spaces0: List<Space>,
    val name: HString,
    val spaces1: List<Space>,
    val colon: Literal,
    val spaces2: List<Space>,
    val value: CookieValue,
    val lt: LineTerminator
) : Node() {

    fun toPair(): Pair<String, String> = name.value to value.value
}

data class CookieQuery(
    override val begin: Position,
    override val end: Position,
    override val type: QueryType,
    val spaces: List<Space>,
    val expr: HString
) : Query()

data class CookiesSection(
    override val begin: Position,
    override val end: Position,
    val lts: List<LineTerminator>,
    val spaces: List<Space>,
    val header: SectionHeader,
    val lt: LineTerminator,
    val cookies: List<Cookie>
) : RequestSection()

data class CookieValue(override val begin: Position, override val end: Position, val value: String) : Node()

data class CountPredicate(
    override val begin: Position,
    override val end: Position,
    override val type: PredicateType,
    val spaces: List<Space>,
    val expr: Number
) : PredicateFunc()

data class DurationQuery(override val begin: Position, override val end: Position, override val type: QueryType) : Query()

data class Entry(override val begin: Position, override val end: Position, val request: Request, val response: Response?) : Node()

data class EqualBoolPredicate(
    override val begin: Position,
    override val end: Position,
    override val type: PredicateType,
    val spaces: List<Space>,
    val expr: Bool
) : PredicateFunc()

data class EqualNullPredicate(
    override val begin: Position,
    override val end: Position,
    override val type: PredicateType,
    val spaces: List<Space>,
    val expr: Null
) : PredicateFunc()

data class EqualNumberPredicate(
    override val begin: Position,
    override val end: Position,
    override val type: PredicateType,
    val spaces: List<Space>,
    val expr: Number
) : PredicateFunc()

data class EqualStringPredicate(
    override val begin: Position,
    override val end: Position,
    override val type: PredicateType,
    val spaces: List<Space>,
    val expr: HString
) : PredicateFunc()

data class EqualExprPredicate(
    override val begin: Position,
    override val end: Position,
    override val type: PredicateType,
    val spaces: List<Space>,
    val expr: Expr
) : PredicateFunc()

data class ExistPredicate(override val begin: Position, override val end: Position, override val type: PredicateType) : PredicateFunc()

data class Expr(
    override val begin: Position,
    override val end: Position,
    val prefix: Literal,
    val name: VariableName,
    val suffix: Literal,
    val text: String,
) : Node()

data class GreaterPredicate(
    override val begin: Position,
    override val end: Position,
    override val type: PredicateType,
    val spaces: List<Space>,
    val expr: Number
) : PredicateFunc()

data class GreaterOrEqualPredicate(
    override val begin: Position,
    override val end: Position,
    override val type: PredicateType,
    val spaces: List<Space>,
    val expr: Number
) : PredicateFunc()

data class File(
    override val begin: Position,
    override val end: Position,
    val prefix: Literal,
    val spaces0: List<Space>,
    val fileName: HString,
    val spaces1: List<Space>,
    val suffix: Literal
) : Bytes()

data class FileParam(
    override val begin: Position,
    override val end: Position,
    val lts: List<LineTerminator>,
    val spaces0: List<Space>,
    val key: HString,
    val spaces1: List<Space>,
    val colon: Literal,
    val spaces2: List<Space>,
    val file: FileValue,
    val lt: LineTerminator
) : Node()

data class FileValue(
    override val begin: Position,
    override val end: Position,
    val prefix: Literal,
    val spaces0: List<Space>,
    val fileName: HString,
    val spaces1: List<Space>,
    val suffix: Literal,
    val spaces2: List<Space>,
    val contentType: HString?
) : Node()

data class FormParamsSection(
    override val begin: Position,
    override val end: Position,
    val lts: List<LineTerminator>,
    val spaces: List<Space>,
    val header: SectionHeader,
    val lt: LineTerminator,
    val params: List<Param>
) : RequestSection()

data class Header(
    override val begin: Position,
    override val end: Position,
    val lts: List<LineTerminator>,
    val spaces: List<Space>,
    val keyValue: KeyValue,
    val lt: LineTerminator
) : Node() {

    val name: String = keyValue.key.value
    val value: String = keyValue.value.value

    fun toPair(): Pair<String, String> = name to value
}

data class HeaderQuery(
    override val begin: Position,
    override val end: Position,
    override val type: QueryType,
    val spaces: List<Space>,
    val headerName: HString
) : Query()

data class HurlFile(
    override val begin: Position,
    override val end: Position,
    val entries: List<Entry>,
    val lts: List<LineTerminator>
) : Node()

data class IncludeBoolPredicate(
    override val begin: Position,
    override val end: Position,
    override val type: PredicateType,
    val spaces: List<Space>,
    val expr: Bool
) : PredicateFunc()

data class IncludeNullPredicate(
    override val begin: Position,
    override val end: Position,
    override val type: PredicateType,
    val spaces: List<Space>,
    val expr: Null
) : PredicateFunc()

data class IncludeNumberPredicate(
    override val begin: Position,
    override val end: Position,
    override val type: PredicateType,
    val spaces: List<Space>,
    val expr: Number
) : PredicateFunc()

data class IncludeStringPredicate(
    override val begin: Position,
    override val end: Position,
    override val type: PredicateType,
    val spaces: List<Space>,
    val expr: HString
) : PredicateFunc()

data class Json(override val begin: Position, override val end: Position, val text: String) : Bytes()

data class JsonPathQuery(
    override val begin: Position,
    override val end: Position,
    override val type: QueryType,
    val spaces: List<Space>,
    val expr: HString
) : Query()

data class KeyValue(
    override val begin: Position,
    override val end: Position,
    val key: HString,
    val spaces0: List<Space>,
    val colon: Literal,
    val spaces1: List<Space>,
    val value: HString
) : Node()

data class LessPredicate(
    override val begin: Position,
    override val end: Position,
    override val type: PredicateType,
    val spaces: List<Space>,
    val expr: Number
) : PredicateFunc()

data class LessOrEqualPredicate(
    override val begin: Position,
    override val end: Position,
    override val type: PredicateType,
    val spaces: List<Space>,
    val expr: Number
) : PredicateFunc()

data class Literal(override val begin: Position, override val end: Position, val value: String) : Node()

data class LineTerminator(
    override val begin: Position,
    override val end: Position,
    val spaces: List<Space>,
    val comment: Comment?,
    val newLine: NewLine?
) : Node()

data class MatchPredicate(
    override val begin: Position,
    override val end: Position,
    override val type: PredicateType,
    val spaces: List<Space>,
    val expr: HString
) : PredicateFunc()

data class Method(override val begin: Position, override val end: Position, val value: String) : Node()

data class MultipartFormDataSection(
    override val begin: Position,
    override val end: Position,
    val lts: List<LineTerminator>,
    val spaces: List<Space>,
    val header: SectionHeader,
    val lt: LineTerminator,
    val params: List<Param>,
    val fileParams: List<FileParam>
) : RequestSection()

data class NewLine(override val begin: Position, override val end: Position, val value: String) : Node()

data class Not(override val begin: Position, override val end: Position, val text: Literal) : Node()

data class Null(override val begin: Position, override val end: Position): Node() {
    val text = "null"
    val value: Any? = null
}

data class Number(override val begin: Position, override val end: Position, val value: Double, val text: String) : Node()

data class Param(
    override val begin: Position,
    override val end: Position,
    val lts: List<LineTerminator>,
    val spaces: List<Space>,
    val keyValue: KeyValue,
    val lt: LineTerminator
) : Node() {

    val name: String = keyValue.key.value
    val value: String = keyValue.value.value

    fun toPair(): Pair<String, String> = name to value
}

data class Predicate(
    override val begin: Position,
    override val end: Position,
    val not: Not?,
    val spaces: List<Space>,
    val predicateFunc: PredicateFunc
) : Node()

sealed class PredicateFunc : Node() {
    abstract val type: PredicateType
}

data class PredicateType(override val begin: Position, override val end: Position, val value: String) : Node()

sealed class Query : Node() {
    abstract val type: QueryType
}

data class QueryType(override val begin: Position, override val end: Position, val value: String) : Node()

data class QueryStringParamsSection(
    override val begin: Position,
    override val end: Position,
    val lts: List<LineTerminator>,
    val spaces: List<Space>,
    val header: SectionHeader,
    val lt: LineTerminator,
    val params: List<Param>
) : RequestSection()

data class RawString(
    override val begin: Position,
    override val end: Position,
    val value: String,
    val text: String
) : Bytes()

data class RegexQuery(
    override val begin: Position,
    override val end: Position,
    override val type: QueryType,
    val spaces: List<Space>,
    val expr: HString
) : Query()

data class RegexSubquery(
    override val begin: Position,
    override val end: Position,
    override val type: SubqueryType,
    val spaces: List<Space>,
    val expr: HString
) : Subquery()

data class Request(
    override val begin: Position,
    override val end: Position,
    val lts: List<LineTerminator>,
    val spaces0: List<Space>,
    val method: Method,
    val spaces1: List<Space>,
    val url: Url,
    val lt: LineTerminator,
    val headers: List<Header>,
    val sections: List<RequestSection>,
    val body: Body?
) : Node() {
    val queryStringParamsSection: QueryStringParamsSection? =
        sections.filterIsInstance<QueryStringParamsSection>().firstOrNull()
    val formParamsSection: FormParamsSection? = sections.filterIsInstance<FormParamsSection>().firstOrNull()
    val cookiesSection: CookiesSection? = sections.filterIsInstance<CookiesSection>().firstOrNull()
    val multipartFormDataSection: MultipartFormDataSection? = sections.filterIsInstance<MultipartFormDataSection>().firstOrNull()
}

sealed class RequestSection: Node()

data class Response(
    override val begin: Position,
    override val end: Position,
    val lts: List<LineTerminator>,
    val spaces0: List<Space>,
    val version: Version,
    val spaces1: List<Space>,
    val status: Status,
    val lt: LineTerminator,
    val headers: List<Header>,
    val sections: List<ResponseSection>,
    val body: Body?
) : Node() {
    val assertsSection: AssertsSection? = sections.filterIsInstance<AssertsSection>().firstOrNull()
    val capturesSection: CapturesSection? = sections.filterIsInstance<CapturesSection>().firstOrNull()
}

sealed class ResponseSection(): Node()

data class Space(override val begin: Position, override val end: Position, val value: String) : Node()

data class StartWithPredicate(
    override val begin: Position,
    override val end: Position,
    override val type: PredicateType,
    val spaces: List<Space>,
    val expr: HString
) : PredicateFunc()

data class Status(override val begin: Position, override val end: Position, val value: Int, val text: String) : Node()

data class StatusQuery(override val begin: Position, override val end: Position, override val type: QueryType) : Query()

sealed class Subquery : Node() {
    abstract val type: SubqueryType
}

data class SubqueryType(override val begin: Position, override val end: Position, val value: String) : Node()

data class Url(override val begin: Position, override val end: Position, val value: String) : Node()

data class VariableQuery(
    override val begin: Position,
    override val end: Position,
    override val type: QueryType,
    val spaces: List<Space>,
    val variable: HString
): Query()

data class Version(override val begin: Position, override val end: Position, val value: String): Node()

data class Xml(override val begin: Position, override val end: Position, val text: String) : Bytes()

data class XPathQuery(
    override val begin: Position,
    override val end: Position,
    override val type: QueryType,
    val spaces: List<Space>,
    val expr: HString
): Query()

data class VariableName(override val begin: Position, override val end: Position, val value: String): Node()

// Node that are not defined in Hurl grammar, used to facilitate parsing.
/**
 * HString (Hurl String) are generic string that holds:
 * - value: the String value of this string
 * - text: the exact string representation of this string (maybe different from the value)
 */
data class HString(override val begin: Position, override val end: Position, val value: String, val text: String) : Node() {
    override fun toString(): String = value
}

data class Base64String(
    override val begin: Position,
    override val end: Position,
    val value: ByteArray,
    val text: String
) : Node()

data class SectionHeader(override val begin: Position, override val end: Position, val value: String) : Node()