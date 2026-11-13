package com.zhangs_tech.runtomo.server.summon

class BannerNotFoundException : RuntimeException("Banner not found")

/** UC-12 4a. [distanceNeededM] is about how far the Player must walk to earn the [shortfall]. */
class NotEnoughCoinsException(val shortfall: Long, val distanceNeededM: Long) :
    RuntimeException("Not enough TomoCoins")
