package com.zhangs_tech.runtomo.server.user

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FriendCodeGeneratorTests {

    @Test
    fun `generates 8 digit codes`() {
        val generator = FriendCodeGenerator()
        repeat(1_000) {
            val code = generator.generate()
            assertTrue(code.matches(Regex("\\d{8}")), "Unexpected friend code: $code")
        }
    }
}
