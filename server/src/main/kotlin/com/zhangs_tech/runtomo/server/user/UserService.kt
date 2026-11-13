package com.zhangs_tech.runtomo.server.user

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(private val userRepository: UserRepository) {
    fun getUser(id: UUID): User = userRepository.findById(id) ?: throw UserNotFoundException()

    fun updateDisplayName(id: UUID, displayName: String): User {
        if (!DisplayName.isValid(displayName)) throw InvalidDisplayNameException()
        return userRepository.updateDisplayName(id, displayName) ?: throw UserNotFoundException()
    }
}
