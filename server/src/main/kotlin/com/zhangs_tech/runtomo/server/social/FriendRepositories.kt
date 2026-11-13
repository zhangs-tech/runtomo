package com.zhangs_tech.runtomo.server.social

import java.time.Instant
import java.util.UUID

data class FriendRequest(val id: UUID, val fromUserId: UUID, val toUserId: UUID, val createdAt: Instant)

interface FriendRepository {
    fun areFriends(userId: UUID, otherUserId: UUID): Boolean
    fun count(userId: UUID): Int
    fun findFriendIds(userId: UUID): List<UUID>
    fun create(userId: UUID, otherUserId: UUID)
    fun delete(userId: UUID, otherUserId: UUID)
}

interface FriendRequestRepository {
    fun findById(id: UUID): FriendRequest?
    fun find(fromUserId: UUID, toUserId: UUID): FriendRequest?

    /** Pending requests to [userId], oldest first. */
    fun findIncoming(userId: UUID): List<FriendRequest>

    /** Does nothing if the same request already exists. */
    fun create(fromUserId: UUID, toUserId: UUID)
    fun delete(id: UUID)
}
