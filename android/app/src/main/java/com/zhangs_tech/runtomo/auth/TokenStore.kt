package com.zhangs_tech.runtomo.auth

interface TokenStore {
    suspend fun read(): AuthTokens?
    suspend fun save(tokens: AuthTokens)
    suspend fun clear()
}
