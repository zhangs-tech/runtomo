package com.zhangs_tech.runtomo.server.auth

import java.time.Instant
import java.util.UUID

data class RefreshToken(
    val id: UUID,
    val userId: UUID,
    // This is the id of the first token in the family.
    val familyId: UUID,
    val tokenHash: String,
    val expiresAt: Instant,
    val revokedAt: Instant?,
    val createdAt: Instant
)
