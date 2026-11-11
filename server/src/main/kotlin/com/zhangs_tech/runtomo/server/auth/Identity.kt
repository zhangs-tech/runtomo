package com.zhangs_tech.runtomo.server.auth

data class Identity(
    val subject: String,
    val email: String?,
    val name: String?,
)
