package com.zhangs_tech.runtomo.tomo

/** UC-02 BR-2. The server checks the same rule. */
object Nickname {
    val LENGTH = 1..16

    fun isValid(nickname: String): Boolean = nickname.trim().length in LENGTH
}
