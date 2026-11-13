package com.zhangs_tech.runtomo.server.user

import kotlin.random.Random

object DisplayName {
    private val LENGTH = 3..20

    fun isValid(name: String): Boolean = name.length in LENGTH && name == name.trim()

    /** UC-01 BR-5: the Google profile name, or "Runner" plus 4 random digits if that name is not valid. */
    fun defaultFor(googleName: String?, random: Random = Random.Default): String =
        googleName?.trim()?.takeIf(::isValid) ?: "Runner%04d".format(random.nextInt(10_000))
}
