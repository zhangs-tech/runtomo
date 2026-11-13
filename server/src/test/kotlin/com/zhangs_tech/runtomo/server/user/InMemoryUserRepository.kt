package com.zhangs_tech.runtomo.server.user

import java.time.Instant
import java.util.UUID

class InMemoryUserRepository : UserRepository {
    val users = mutableListOf<User>()

    override fun findById(id: UUID): User? = users.find { it.id == id }

    override fun lockById(id: UUID): User? = findById(id)

    override fun updateDisplayName(id: UUID, displayName: String): User? =
        update(id) { it.copy(displayName = displayName) }

    override fun setEquippedTomo(id: UUID, tomoId: UUID) {
        update(id) { it.copy(equippedTomoId = tomoId) }
    }

    private fun update(id: UUID, change: (User) -> User): User? {
        val index = users.indexOfFirst { it.id == id }
        if (index < 0) return null
        users[index] = change(users[index])
        return users[index]
    }

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
