package com.zhangs_tech.runtomo.server.auth

import java.time.Instant
import java.util.UUID

interface RefreshTokenRepository {
    fun findByTokenHash(tokenHash: String): RefreshToken?

    fun createFamily(userId: UUID, tokenHash: String, expiresAt: Instant): RefreshToken

    fun create(
        userId: UUID,
        familyId: UUID,
        tokenHash: String,
        expiresAt: Instant
    ): RefreshToken

    /** Returns false when the token was already revoked. */
    fun revoke(id: UUID): Boolean

    fun revokeFamily(familyId: UUID)
}