package com.zhangs_tech.runtomo.server.auth.dto

data class RefreshTokenResponse(
    val accessToken: String,
    val accessTokenExpiresIn: Long,
    val refreshToken: String
)
