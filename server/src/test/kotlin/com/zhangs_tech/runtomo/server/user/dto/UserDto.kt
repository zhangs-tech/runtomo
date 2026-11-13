package com.zhangs_tech.runtomo.server.user.dto

import java.util.UUID

data class UserDto(
    val id: UUID,
    val displayName: String,
    val friendCode: String,
)
