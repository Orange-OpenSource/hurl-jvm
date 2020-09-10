package com.orange.ccmd.hurl.core.variable

sealed class Var {
    abstract val name: String
}

data class StringVar(override val name: String, val value: String) : Var()

data class NumberVar(override val name: String, val value: Number) : Var()

data class BoolVar(override val name: String, val value: Boolean) : Var()

data class ObjectVar(override val name: String, val value: Any?): Var()