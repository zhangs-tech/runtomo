package com.zhangs_tech.runtomo.server.social

import java.time.Instant
import java.util.UUID

class InMemoryFriendRepository : FriendRepository {
    val pairs = mutableSetOf<Set<UUID>>()

    override fun areFriends(userId: UUID, otherUserId: UUID): Boolean = setOf(userId, otherUserId) in pairs
    override fun count(userId: UUID): Int = pairs.count { userId in it }
    override fun findFriendIds(userId: UUID): List<UUID> = pairs.filter { userId in it }.map { (it - userId).single() }
    override fun create(userId: UUID, otherUserId: UUID) {
        pairs += setOf(userId, otherUserId)
    }
    override fun delete(userId: UUID, otherUserId: UUID) {
        pairs -= setOf(userId, otherUserId)
    }
}

class InMemoryFriendRequestRepository : FriendRequestRepository {
    val requests = mutableListOf<FriendRequest>()

    override fun findById(id: UUID): FriendRequest? = requests.find { it.id == id }
    override fun find(fromUserId: UUID, toUserId: UUID): FriendRequest? =
        requests.find { it.fromUserId == fromUserId && it.toUserId == toUserId }
    override fun findIncoming(userId: UUID): List<FriendRequest> = requests.filter { it.toUserId == userId }
    override fun create(fromUserId: UUID, toUserId: UUID) {
        if (find(fromUserId, toUserId) == null) requests += FriendRequest(UUID.randomUUID(), fromUserId, toUserId, Instant.now())
    }
    override fun delete(id: UUID) {
        requests.removeIf { it.id == id }
    }
}
