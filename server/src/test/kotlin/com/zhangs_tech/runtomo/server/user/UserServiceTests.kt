package com.zhangs_tech.runtomo.server.user

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class UserServiceTests {
    private val userRepository = InMemoryUserRepository()
    private val userService = UserService(userRepository)
    private val user = userRepository.create("sub-1", "Runner0001", "00000001")!!

    @Test
    fun `update display name saves a valid name`() {
        assertEquals("Kai", userService.updateDisplayName(user.id, "Kai").displayName)
        assertEquals("Kai", userRepository.findById(user.id)!!.displayName)
    }

    @Test
    fun `update display name rejects a name that is too short`() {
        assertThrows<InvalidDisplayNameException> { userService.updateDisplayName(user.id, "Al") }
    }

    @Test
    fun `get user fails for an unknown user`() {
        assertThrows<UserNotFoundException> { userService.getUser(UUID.randomUUID()) }
    }
}
