package com.zhangs_tech.runtomo.user

/** UC-01 BR-1. The server checks the same rule. */
object DisplayName {
    val LENGTH = 3..20

    fun isValid(name: String): Boolean = name.trim().length in LENGTH
}
