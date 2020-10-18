package com.orange.ccmd.hurl.core.predicate

import com.orange.ccmd.hurl.core.run.QueryBooleanResult
import com.orange.ccmd.hurl.core.run.QueryListResult
import com.orange.ccmd.hurl.core.run.QueryNodeSetResult
import com.orange.ccmd.hurl.core.run.QueryNoneResult
import com.orange.ccmd.hurl.core.run.QueryNumberResult
import com.orange.ccmd.hurl.core.run.QueryObjectResult
import com.orange.ccmd.hurl.core.run.QueryStringResult
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals

class PredicateEvalTest {

    private fun PredicateResult.testName(): String {
        return "$first $second $succeeded"
    }

    @TestFactory
    fun `evaluate predicate`() = listOf(

        equalString(QueryStringResult("toto"), "toto") to true,
        equalString(QueryBooleanResult(true), "toto") to false,
        notEqualString(QueryStringResult("toto"), "tata") to true,
        notEqualString(QueryNumberResult(1), "tata") to true,

        equalBool(QueryBooleanResult(true), true) to true,
        equalBool(QueryStringResult("toto"), true) to false,
        notEqualBool(QueryBooleanResult(false), true) to true,
        notEqualBool(QueryStringResult("toto"), true) to true,

        equalNull(QueryObjectResult(null)) to true,
        equalNull(QueryStringResult("toto")) to false,
        notEqualNull(QueryObjectResult(null)) to false,
        notEqualNull(QueryStringResult("toto")) to true,

        equalDouble(QueryNumberResult(2), 2.0) to true,
        equalDouble(QueryStringResult("toto"), 2.0) to false,
        notEqualDouble(QueryNumberResult(2), 2.0) to false,
        notEqualDouble(QueryStringResult("toto"), 2.0) to true,

        contain(QueryStringResult("toto"), "to") to true,
        contain(QueryNumberResult(2), "to") to false,
        notContain(QueryStringResult("toto"), "to") to false,
        notContain(QueryNumberResult(2), "to") to false,
        notContain(QueryNoneResult, "to") to true,

        includeNull(QueryListResult(listOf(1, null))) to true,
        includeNull(QueryNumberResult(2)) to false,
        notIncludeNull(QueryListResult(listOf(1, null))) to false,
        notIncludeNull(QueryNumberResult(2)) to false,
        notIncludeNull(QueryNoneResult) to true,

        includeNumber(QueryListResult(listOf(1, null)), 1.0) to true,
        includeNumber(QueryNumberResult(2), 1.0) to false,
        notIncludeNumber(QueryListResult(listOf(1, null)), 1.0) to false,
        notIncludeNumber(QueryNumberResult(2), 1.0) to false,
        notIncludeNumber(QueryNoneResult, 1.0) to true,

        includeBool(QueryListResult(listOf(1, true)), true) to true,
        includeBool(QueryNumberResult(2), true) to false,
        notIncludeBool(QueryListResult(listOf(1, true)), true) to false,
        notIncludeBool(QueryNumberResult(2), true) to false,
        notIncludeBool(QueryNoneResult, true) to true,

        includeString(QueryListResult(listOf(1, "toto")), "toto") to true,
        includeString(QueryNumberResult(2), "toto") to false,
        notIncludeString(QueryListResult(listOf(1, "toto")), "toto") to false,
        notIncludeString(QueryNumberResult(2), "toto") to false,
        notIncludeString(QueryNoneResult, "toto") to true,

        count(QueryListResult(listOf(1, true)), 2.0) to true,
        count(QueryNodeSetResult(size = 2), 2.0) to true,
        count(QueryStringResult("toto"), 2.0) to false,
        notCount(QueryListResult(listOf(1, true)), 2.0) to false,
        notCount(QueryNodeSetResult(size = 2), 2.0) to false,
        notCount(QueryStringResult("toto"), 2.0) to false,
        notCount(QueryNoneResult, 2.0) to true,

        startWith(QueryStringResult("toto"), "to") to true,
        startWith(QueryNumberResult(2), "to") to false,
        notStartWith(QueryStringResult("toto"), "to") to false,
        notStartWith(QueryNumberResult(2), "to") to false,
        notStartWith(QueryNoneResult, "to") to true,

        match(QueryStringResult("toto"), "to") to true,
        match(QueryNumberResult(2), "to") to false,
        notMatch(QueryStringResult("toto"), "to") to false,
        notMatch(QueryNumberResult(2), "to") to false,
        notMatch(QueryNoneResult, "to") to true,

        exist(QueryNoneResult) to false,
        exist(QueryNodeSetResult(size = 0)) to false,
        exist(QueryNodeSetResult(size = 42)) to true,
        exist(QueryStringResult("toto")) to true,
        notExist(QueryNoneResult) to true,
        notExist(QueryNodeSetResult(size = 0)) to true,
        notExist(QueryNodeSetResult(size = 42)) to false,
        notExist(QueryStringResult("toto")) to false,
    ).map { (expr, expectedResult) ->
        DynamicTest.dynamicTest(expr.testName()) {
            assertEquals(expr.succeeded, expectedResult)
        }

    }
}
