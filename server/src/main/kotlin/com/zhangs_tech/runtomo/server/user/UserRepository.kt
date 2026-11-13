package com.zhangs_tech.runtomo.server.user

import java.util.UUID

interface UserRepository {
    fun findById(id: UUID): User?
    fun findByAuthSubject(authSubject: String): User?
    fun findByFriendCode(friendCode: String): User?
    /** Returns null when [friendCode] is already taken. */
    fun create(authSubject: String, displayName: String, friendCode: String): User?
    /** Locks the user row until the transaction ends. */
    fun lockById(id: UUID): User?
    fun updateDisplayName(id: UUID, displayName: String): User?
    fun setEquippedTomo(id: UUID, tomoId: UUID)
}
