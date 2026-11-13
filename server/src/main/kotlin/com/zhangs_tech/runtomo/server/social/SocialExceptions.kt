package com.zhangs_tech.runtomo.server.social

class PlayerNotFoundException : RuntimeException("No player with this code")

class OwnFriendCodeException : RuntimeException("This is your own friend code")

class AlreadyFriendsException : RuntimeException("You are already friends")

/** UC-15 BR-1. */
class FriendLimitException(max: Int) : RuntimeException("A player can have at most $max friends")

class InvalidPeriodException : RuntimeException("Period must be today, week or month")

class FriendRequestNotFoundException : RuntimeException("Friend request not found")
