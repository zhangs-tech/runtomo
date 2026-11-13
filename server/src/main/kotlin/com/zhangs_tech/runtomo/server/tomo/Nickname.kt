package com.zhangs_tech.runtomo.server.tomo

object Nickname {
    private val LENGTH = 1..16

    fun isValid(nickname: String): Boolean = nickname.length in LENGTH && nickname == nickname.trim()
}
