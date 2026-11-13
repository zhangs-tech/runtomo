package com.zhangs_tech.runtomo.server.user

import java.time.Instant
import java.util.UUID

class InMemoryUserRepository : UserRepository {
    val users = mutableListOf<User>()

    override fun findById(id: UUID): User? = users.find { it.id == id }

    override fun findByAuthSubject(authSubject: String): User? = users.find { it.authSubject == authSubject }

    override fun create(authSubject: String, displayName: String, friendCode: String): User? {
        if (users.any { it.friendCode == friendCode }) return null
        val user = User(
            id = UUID.randomUUID(),
            authSubject = authSubject,
            displayName = displayName,
            friendCode = friendCode,
            coinBalance = 0,
            equippedTomoId = null,
            createdAt = Instant.now()
        )
        users += user
        return user
    }
}
