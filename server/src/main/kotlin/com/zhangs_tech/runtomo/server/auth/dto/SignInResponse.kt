package com.zhangs_tech.runtomo.server.auth.dto

data class SignInResponse(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresIn: Long,
    val isNewUser: Boolean
)
