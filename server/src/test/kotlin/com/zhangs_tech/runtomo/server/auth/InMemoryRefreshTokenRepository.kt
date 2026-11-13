package com.zhangs_tech.runtomo.server.auth

import java.time.Instant
import java.util.UUID

class InMemoryRefreshTokenRepository : RefreshTokenRepository {
    val tokens = mutableListOf<RefreshToken>()

    override fun findByTokenHash(tokenHash: String): RefreshToken? = tokens.find { it.tokenHash == tokenHash }

    override fun createFamily(userId: UUID, tokenHash: String, expiresAt: Instant): RefreshToken {
        val id = UUID.randomUUID()
        return insert(id, userId, id, tokenHash, expiresAt)
    }

    override fun create(userId: UUID, familyId: UUID, tokenHash: String, expiresAt: Instant): RefreshToken =
        insert(UUID.randomUUID(), userId, familyId, tokenHash, expiresAt)

    override fun revoke(id: UUID): Boolean {
        val index = tokens.indexOfFirst { it.id == id && it.revokedAt == null }
        if (index == -1) return false
        tokens[index] = tokens[index].copy(revokedAt = Instant.now())
        return true
    }

    override fun revokeFamily(familyId: UUID) {
        tokens.replaceAll {
            if (it.familyId == familyId && it.revokedAt == null) it.copy(revokedAt = Instant.now()) else it
        }
    }

    private fun insert(id: UUID, userId: UUID, familyId: UUID, tokenHash: String, expiresAt: Instant): RefreshToken {
        val token = RefreshToken(
            id = id,
            userId = userId,
            familyId = familyId,
            tokenHash = tokenHash,
            expiresAt = expiresAt,
            revokedAt = null,
            createdAt = Instant.now()
        )
        tokens += token
        return token
    }
}
