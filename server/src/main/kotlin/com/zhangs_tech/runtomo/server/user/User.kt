package com.zhangs_tech.runtomo.server.user

import java.math.BigInteger
import java.util.UUID
import java.time.Instant

data class User(
    val id: UUID,
    val authSubject: String,
    val displayName: String,
    val friendCode: String,
    val coinBalance: Long,
    val equippedTomoId: UUID?,
    val createdAt: Instant
)
