package com.zhangs_tech.runtomo.activity

/** Runs the step sync about every 15 minutes while the app is closed (UC-03 BR-7). */
interface BackgroundSync {
    fun schedule()
    fun cancel()
}
