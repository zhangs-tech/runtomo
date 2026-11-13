package com.zhangs_tech.runtomo.server.coin

/** UC-12 4a, UC-18 2a. [distanceNeededM] is about how far the Player must walk to earn the [shortfall]. */
class NotEnoughCoinsException(val shortfall: Long, val distanceNeededM: Long) :
    RuntimeException("Not enough TomoCoins")
