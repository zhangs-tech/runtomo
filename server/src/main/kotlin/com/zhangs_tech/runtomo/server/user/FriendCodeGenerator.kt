package com.zhangs_tech.runtomo.server.user

import org.springframework.stereotype.Component
import java.security.SecureRandom

@Component
class FriendCodeGenerator {
    private val random = SecureRandom()

    fun generate(): String = "%0${LENGTH}d".format(random.nextInt(UPPER_BOUND))

    private companion object {
        const val LENGTH = 8
        const val UPPER_BOUND = 100_000_000
    }
}
