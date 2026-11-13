package com.zhangs_tech.runtomo.server.auth

class InvalidRefreshTokenException : RuntimeException("Refresh token is invalid or expired")
