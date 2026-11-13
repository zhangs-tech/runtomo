package com.zhangs_tech.runtomo.server.user

/** The access token is valid, but its user no longer exists. */
class UserNotFoundException : RuntimeException("User not found")
