package com.zhangs_tech.runtomo.activity

import java.time.Instant

/** Reads the Player's steps from the device (UC-03). */
interface StepsReader {
    /** The permissions to request together: read steps, and read them in the background. */
    val permissions: Set<String>

    suspend fun canReadSteps(): Boolean

    /** True if the device supports background reads and the Player allowed them (UC-03 1b). */
    suspend fun canReadInBackground(): Boolean

    /** Total steps from every source, with duplicates counted once (UC-03 BR-4). */
    suspend fun readSteps(start: Instant, end: Instant): Int
}
