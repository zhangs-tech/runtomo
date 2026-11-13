package com.zhangs_tech.runtomo.server.user

import java.util.UUID

interface UserRepository {
    fun findById(id: UUID): User?
    fun findByAuthSubject(authSubject: String): User?
    /** Returns null when [friendCode] is already taken. */
    fun create(authSubject: String, displayName: String, friendCode: String): User?
}