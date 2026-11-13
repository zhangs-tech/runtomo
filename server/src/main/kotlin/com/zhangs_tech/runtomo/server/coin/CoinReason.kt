package com.zhangs_tech.runtomo.server.coin

/** Matches the `coin_transactions.reason` check constraint. */
enum class CoinReason(val dbValue: String) {
    ACTIVITY("activity"),
    QUEST("quest"),
    SUMMON("summon"),
    DUPLICATE("duplicate"),
    FOOD("food"),
}
