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

import java.io.File

sealed class Node(val begin: Position, val end: Position)

/**
 * From https://hurl.dev/docs/grammar.html.
 */

class Assert(
    begin: Position,
    end: Position,
    val lts: List<LineTerminator>,
    val spaces0: List<Space>,
    val query: Query,
    val spaces1: List<Space>,
    val predicate: Predicate,
    val lt: LineTerminator
) : Node(begin, end)

class AssertsSection(
    begin: Position,
    end: Position,
    val lts: List<LineTerminator>,
    val spaces: List<Space>,
    val header: SectionHeader,
    val lt: LineTerminator,
    val asserts: List<Assert>
) : ResponseSection(begin, end)

class Base64(
    begin: Position,
    end: Position,
    val prefix: Literal,
    val spaces0: List<Space>,
    val base64String: Base64String,
    val spaces1: List<Space>,
    val suffix: Literal
) : Bytes(begin, end)

class Body(
    begin: Position,
    end: Position,
    val lts: List<LineTerminator>,
    val spaces: List<Space>,
    val bytes: Bytes,
    val lt: LineTerminator
) : Node(begin, end)

class BodyQuery(
    begin: Position,
    end: Position,
    type: QueryType
) : Query(begin, end, type)

class Bool(begin: Position, end: Position, val value: Boolean, val text: String) : Node(begin, end)

sealed class Bytes(begin: Position, end: Position): Node(begin, end)

class Capture(
    begin: Position,
    end: Position,
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
) : Node(begin, end)

class CapturesSection(
    begin: Position,
    end: Position,
    val lts: List<LineTerminator>,
    val spaces: List<Space>,
    val header: SectionHeader,
    val lt: LineTerminator,
    val captures: List<Capture>
) : ResponseSection(begin, end)

class Comment(begin: Position, end: Position, val value: String) : Node(begin, end)

class ContainPredicate(
    begin: Position,
    end: Position,
    type: PredicateType,
    val spaces: List<Space>,
    val expr: HString
) : PredicateFunc(begin, end, type)

class Cookie(
    begin: Position,
    end: Position,
    val lts: List<LineTerminator>,
    val spaces0: List<Space>,
    val name: HString,
    val spaces1: List<Space>,
    val colon: Literal,
    val spaces2: List<Space>,
    val value: CookieValue,
    val lt: LineTerminator
) : Node(begin, end) {

    fun toPair(): Pair<String, String> = name.value to value.value
}

class CookieQuery(
    begin: Position,
    end: Position,
    type: QueryType,
    val spaces: List<Space>,
    val expr: HString
) : Query(begin, end, type)

class CookiesSection(
    begin: Position,
    end: Position,
    val lts: List<LineTerminator>,
    val spaces: List<Space>,
    val header: SectionHeader,
    val lt: LineTerminator,
    val cookies: List<Cookie>
) : RequestSection(begin, end)

class CookieValue(begin: Position, end: Position, val value: String) : Node(begin, end)

class CountPredicate(
    begin: Position,
    end: Position,
    type: PredicateType,
    val spaces: List<Space>,
    val expr: Number
) : PredicateFunc(begin, end, type)

class Entry(begin: Position, end: Position, val request: Request, val response: Response?) : Node(begin, end)

class EqualBoolPredicate(
    begin: Position,
    end: Position,
    type: PredicateType,
    val spaces: List<Space>,
    val expr: Bool
) : PredicateFunc(begin, end, type)

class EqualNumberPredicate(
    begin: Position,
    end: Position,
    type: PredicateType,
    val spaces: List<Space>,
    val expr: Number
) : PredicateFunc(begin, end, type)

class EqualStringPredicate(
    begin: Position,
    end: Position,
    type: PredicateType,
    val spaces: List<Space>,
    val expr: HString
) : PredicateFunc(begin, end, type)

class ExistPredicate(begin: Position, end: Position, type: PredicateType) : PredicateFunc(begin, end, type)

class File(
    begin: Position,
    end: Position,
    val prefix: Literal,
    val spaces0: List<Space>,
    val fileName: HString,
    val spaces1: List<Space>,
    val suffix: Literal
) : Bytes(begin, end)

class FileParam(
    begin: Position,
    end: Position,
    val lts: List<LineTerminator>,
    val spaces0: List<Space>,
    val key: HString,
    val spaces1: List<Space>,
    val colon: Literal,
    val spaces2: List<Space>,
    val file: FileValue,
    val lt: LineTerminator
) : Node(begin, end)

class FileValue(
    begin: Position,
    end: Position,
    val prefix: Literal,
    val spaces0: List<Space>,
    val fileName: HString,
    val spaces1: List<Space>,
    val suffix: Literal,
    val spaces2: List<Space>,
    val contentType: HString?
) : Node(begin, end)

class FormParamsSection(
    begin: Position,
    end: Position,
    val lts: List<LineTerminator>,
    val spaces: List<Space>,
    val header: SectionHeader,
    val lt: LineTerminator,
    val params: List<Param>
) : RequestSection(begin, end)

class Header(
    begin: Position,
    end: Position,
    val lts: List<LineTerminator>,
    val spaces: List<Space>,
    val keyValue: KeyValue,
    val lt: LineTerminator
) : Node(begin, end) {

    val name: String = keyValue.key.value
    val value: String = keyValue.value.value

    fun toPair(): Pair<String, String> = name to value
}

class HeaderQuery(
    begin: Position,
    end: Position,
    type: QueryType,
    val spaces: List<Space>,
    val headerName: HString
) : Query(begin, end, type)

class HurlFile(
    begin: Position,
    end: Position,
    val entries: List<Entry>,
    val lts: List<LineTerminator>
) : Node(begin, end)

class Json(begin: Position, end: Position, val text: String) : Bytes(begin, end)

class JsonPathQuery(
    begin: Position,
    end: Position,
    type: QueryType,
    val spaces: List<Space>,
    val expr: HString
) : Query(begin, end, type)

class KeyValue(
    begin: Position,
    end: Position,
    val key: HString,
    val spaces0: List<Space>,
    val colon: Literal,
    val spaces1: List<Space>,
    val value: HString
) : Node(begin, end)

class Literal(begin: Position, end: Position, val value: String) : Node(begin, end)

class LineTerminator(
    begin: Position,
    end: Position,
    val spaces: List<Space>,
    val comment: Comment?,
    val newLine: NewLine?
) : Node(begin, end)

class MatchPredicate(
    begin: Position,
    end: Position,
    type: PredicateType,
    val spaces: List<Space>,
    val expr: HString
) : PredicateFunc(begin, end, type)

class Method(begin: Position, end: Position, val value: String) : Node(begin, end)

class MultipartFormDataSection(
    begin: Position,
    end: Position,
    val lts: List<LineTerminator>,
    val spaces: List<Space>,
    val header: SectionHeader,
    val lt: LineTerminator,
    val params: List<Param>,
    val fileParams: List<FileParam>
) : RequestSection(begin, end)

class NewLine(begin: Position, end: Position, val value: String) : Node(begin, end)

class Not(begin: Position, end: Position, val text: Literal) : Node(begin, end)

class Number(begin: Position, end: Position, val value: Double, val text: String) : Node(begin, end)

class Param(
    begin: Position,
    end: Position,
    val lts: List<LineTerminator>,
    val spaces: List<Space>,
    val keyValue: KeyValue,
    val lt: LineTerminator
) : Node(begin, end) {

    val name: String = keyValue.key.value
    val value: String = keyValue.value.value

    fun toPair(): Pair<String, String> = name to value
}

class Predicate(
    begin: Position,
    end: Position,
    val not: Not?,
    val spaces: List<Space>,
    val predicateFunc: PredicateFunc
) : Node(begin, end) {
    val expr: Any? = when (predicateFunc) {
        is EqualStringPredicate -> predicateFunc.expr.value
        is EqualNumberPredicate -> predicateFunc.expr.value
        is EqualBoolPredicate -> predicateFunc.expr.value
        is CountPredicate -> predicateFunc.expr.value
        is StartWithPredicate -> predicateFunc.expr.value
        is ContainPredicate -> predicateFunc.expr.value
        is MatchPredicate -> predicateFunc.expr.value
        is ExistPredicate -> null
    }

}

sealed class PredicateFunc(begin: Position, end: Position, val type: PredicateType) : Node(begin, end)

class PredicateType(begin: Position, end: Position, val value: String) : Node(begin, end)

sealed class Query(begin: Position, end: Position, val type: QueryType) : Node(begin, end)

class QueryType(begin: Position, end: Position, val value: String) : Node(begin, end)

class QueryStringParamsSection(
    begin: Position,
    end: Position,
    val lts: List<LineTerminator>,
    val spaces: List<Space>,
    val header: SectionHeader,
    val lt: LineTerminator,
    val params: List<Param>
) : RequestSection(begin, end)

class RawString(
    begin: Position,
    end: Position,
    val value: String,
    val text: String
) : Bytes(begin, end)

class RegexQuery(
    begin: Position,
    end: Position,
    type: QueryType,
    val spaces: List<Space>,
    val expr: HString
) : Query(begin, end, type)

class RegexSubquery(
    begin: Position,
    end: Position,
    type: SubqueryType,
    val spaces: List<Space>,
    val expr: HString
) : Subquery(begin, end, type)

class Request(
    begin: Position,
    end: Position,
    val lts: List<LineTerminator>,
    val spaces0: List<Space>,
    val method: Method,
    val spaces1: List<Space>,
    val url: Url,
    val lt: LineTerminator,
    val headers: List<Header>,
    val sections: List<RequestSection>,
    val body: Body?
) : Node(begin, end) {
    val queryStringParamsSection: QueryStringParamsSection? =
        sections.filterIsInstance<QueryStringParamsSection>().firstOrNull()
    val formParamsSection: FormParamsSection? = sections.filterIsInstance<FormParamsSection>().firstOrNull()
    val cookiesSection: CookiesSection? = sections.filterIsInstance<CookiesSection>().firstOrNull()
    val multipartFormDataSection: MultipartFormDataSection? = sections.filterIsInstance<MultipartFormDataSection>().firstOrNull()
}

sealed class RequestSection(begin: Position, end: Position): Node(begin, end)

class Response(
    begin: Position,
    end: Position,
    val lts: List<LineTerminator>,
    val spaces0: List<Space>,
    val version: Version,
    val spaces1: List<Space>,
    val status: Status,
    val lt: LineTerminator,
    val headers: List<Header>,
    val sections: List<ResponseSection>,
    val body: Body?
) : Node(begin, end) {
    val assertsSection: AssertsSection? = sections.filterIsInstance<AssertsSection>().firstOrNull()
    val capturesSection: CapturesSection? = sections.filterIsInstance<CapturesSection>().firstOrNull()
}

sealed class ResponseSection(begin: Position, end: Position): Node(begin, end)

class Space(begin: Position, end: Position, val value: String) : Node(begin, end)

class StartWithPredicate(
    begin: Position,
    end: Position,
    type: PredicateType,
    val spaces: List<Space>,
    val expr: HString
) : PredicateFunc(begin, end, type)

class Status(begin: Position, end: Position, val value: Int, val text: String) : Node(begin, end)

class StatusQuery(begin: Position, end: Position, type: QueryType) : Query(begin, end, type)

sealed class Subquery(begin: Position, end: Position, val type: SubqueryType) : Node(begin, end)

class SubqueryType(begin: Position, end: Position, val value: String) : Node(begin, end)

class Url(begin: Position, end: Position, val value: String) : Node(begin, end)

class VariableQuery(begin: Position, end: Position, type: QueryType, val spaces: List<Space>, val variable: HString): Query(begin, end, type)

class Version(begin: Position, end: Position, val value: String): Node(begin, end)

class Xml(begin: Position, end: Position, val text: String) : Bytes(begin, end)

class XPathQuery(begin: Position, end: Position, type: QueryType, val spaces: List<Space>, val expr: HString): Query(begin, end, type)

// Node that are not defined in Hurl grammar, used to facilitate parsing.
/**
 * HString (Hurl String) are generic string that holds:
 * - value: the String value of this string
 * - text: the exact string representation of this string (maybe different from the value)
 */
class HString(begin: Position, end: Position, val value: String, val text: String) : Node(begin, end) {
    override fun toString(): String = value
}

class Base64String(
    begin: Position,
    end: Position,
    val value: ByteArray,
    val text: String
) : Node(begin, end)

class SectionHeader(begin: Position, end: Position, val value: String) : Node(begin, end)