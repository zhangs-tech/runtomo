package com.zhangs_tech.runtomo.server.coin

import java.util.UUID

interface CoinRepository {
    /**
     * Adds a ledger row and changes `users.coin_balance` by [amount] (negative to spend).
     * Returns the new balance.
     */
    fun record(userId: UUID, amount: Int, reason: CoinReason, refId: UUID?): Long
}
