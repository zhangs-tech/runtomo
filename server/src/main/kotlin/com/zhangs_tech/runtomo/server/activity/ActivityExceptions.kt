package com.zhangs_tech.runtomo.server.activity

/** UC-03 BR-2: only today's steps count. */
class DayClosedException : RuntimeException("Only today's steps can be synced")

class InvalidStepsException(max: Int) : RuntimeException("Steps must be 0 to $max")
