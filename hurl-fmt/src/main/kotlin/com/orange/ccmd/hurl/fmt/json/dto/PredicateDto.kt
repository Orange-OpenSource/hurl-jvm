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

package com.orange.ccmd.hurl.fmt.json.dto

import com.orange.ccmd.hurl.core.ast.ContainPredicate
import com.orange.ccmd.hurl.core.ast.CountPredicate
import com.orange.ccmd.hurl.core.ast.EqualBoolPredicate
import com.orange.ccmd.hurl.core.ast.EqualExprPredicate
import com.orange.ccmd.hurl.core.ast.EqualNullPredicate
import com.orange.ccmd.hurl.core.ast.EqualNumberPredicate
import com.orange.ccmd.hurl.core.ast.EqualStringPredicate
import com.orange.ccmd.hurl.core.ast.ExistPredicate
import com.orange.ccmd.hurl.core.ast.GreaterOrEqualPredicate
import com.orange.ccmd.hurl.core.ast.GreaterPredicate
import com.orange.ccmd.hurl.core.ast.IncludeBoolPredicate
import com.orange.ccmd.hurl.core.ast.IncludeNullPredicate
import com.orange.ccmd.hurl.core.ast.IncludeNumberPredicate
import com.orange.ccmd.hurl.core.ast.IncludeStringPredicate
import com.orange.ccmd.hurl.core.ast.LessOrEqualPredicate
import com.orange.ccmd.hurl.core.ast.LessPredicate
import com.orange.ccmd.hurl.core.ast.MatchPredicate
import com.orange.ccmd.hurl.core.ast.Predicate
import com.orange.ccmd.hurl.core.ast.StartWithPredicate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
sealed class PredicateDto

@Serializable
@SerialName("contain")
data class ContainPredicateDto(
    val not: Boolean? = null,
    val value: String,
) : PredicateDto()

@Serializable
@SerialName("count")
data class CountPredicateDto(
    val not: Boolean? = null,
    val value: Long,
) : PredicateDto()

@Serializable
@SerialName("equal")
data class EqualPredicateDto(
    val not: Boolean? = null,
    val value: ValueDto,
) : PredicateDto()


@Serializable
@SerialName("exist")
data class ExistPredicateDto(
    val not: Boolean? = null,
) : PredicateDto()

@Serializable
@SerialName("include")
data class IncludePredicateDto(
    val not: Boolean? = null,
    val value: ValueDto,
) : PredicateDto()

@Serializable
@SerialName("match")
data class MatchPredicateDto(
    val not: Boolean? = null,
    val value: String,
) : PredicateDto()

@Serializable
@SerialName("start-with")
data class StartWithPredicateDto(
    val not: Boolean? = null,
    val value: String,
) : PredicateDto()

@Serializable
@SerialName("greater")
data class GreaterPredicateDto(
    val not: Boolean? = null,
    val value: ValueDto,
) : PredicateDto()

@Serializable
@SerialName("greater-or-equal")
data class GreaterOrEqualPredicateDto(
    val not: Boolean? = null,
    val value: ValueDto,
) : PredicateDto()

@Serializable
@SerialName("less")
data class LessPredicateDto(
    val not: Boolean? = null,
    val value: ValueDto,
) : PredicateDto()

@Serializable
@SerialName("less-or-equal")
data class LessOrEqualPredicateDto(
    val not: Boolean? = null,
    val value: ValueDto,
) : PredicateDto()

fun Predicate.toPredicateDto(): PredicateDto {
    val notValue = not?.let { true }
    return when (val pred = predicateFunc) {
        is ContainPredicate -> ContainPredicateDto(not = notValue, value = pred.expr.value)
        is CountPredicate -> CountPredicateDto(not = notValue, value = pred.expr.value.toLong())
        is EqualBoolPredicate -> EqualPredicateDto(not = notValue, value = BooleanValueDto(pred.expr.value))
        is EqualNullPredicate -> EqualPredicateDto(not = notValue, value = NullValueDto)
        is EqualNumberPredicate -> EqualPredicateDto(not = notValue, value = numberDto(text = pred.expr.text, value = pred.expr.value))
        is EqualStringPredicate -> EqualPredicateDto(not = notValue, value = StringValueDto(pred.expr.value))
        is EqualExprPredicate -> EqualPredicateDto(not = notValue, value = StringValueDto(pred.expr.name.value))
        is ExistPredicate -> ExistPredicateDto(not = notValue)
        is IncludeBoolPredicate -> IncludePredicateDto(not = notValue, value = BooleanValueDto(pred.expr.value))
        is IncludeNullPredicate -> IncludePredicateDto(not = notValue, value = NullValueDto)
        is IncludeNumberPredicate -> IncludePredicateDto(not = notValue, value = numberDto(text = pred.expr.text, value = pred.expr.value))
        is IncludeStringPredicate -> IncludePredicateDto(not = notValue, value = StringValueDto(pred.expr.value))
        is MatchPredicate -> MatchPredicateDto(not = notValue, value = pred.expr.value)
        is StartWithPredicate -> StartWithPredicateDto(not = notValue, value = pred.expr.value)
        is GreaterPredicate -> GreaterPredicateDto(not = notValue, value = numberDto(text = pred.expr.text, value = pred.expr.value))
        is GreaterOrEqualPredicate -> GreaterOrEqualPredicateDto(not = notValue, value = numberDto(text = pred.expr.text, value = pred.expr.value))
        is LessPredicate -> LessPredicateDto(not = notValue, value = numberDto(text = pred.expr.text, value = pred.expr.value))
        is LessOrEqualPredicate -> LessOrEqualPredicateDto(not = notValue, value = numberDto(text = pred.expr.text, value = pred.expr.value))
    }
}

// FIXME: currently in the ast, we don't differentiate double and long values.
//  For the moment, to serialize a value as an int when the value is an int in the ast, we
//  check if the text representation of the value contains a dot (will be done at ast
//  parsing time in the future)
private fun numberDto(text: String, value: Double): ValueDto {
    return if ("." in text) {
        DoubleValueDto(value)
    } else {
        LongValueDto(value.toLong())
    }
}