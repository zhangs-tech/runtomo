package com.zhangs_tech.runtomo.server.coin

import com.zhangs_tech.runtomo.server.user.InMemoryUserRepository
import java.util.UUID

class InMemoryCoinRepository(private val userRepository: InMemoryUserRepository) : CoinRepository {
    data class Transaction(val userId: UUID, val amount: Int, val reason: CoinReason, val refId: UUID?)

    val transactions = mutableListOf<Transaction>()

    override fun record(userId: UUID, amount: Int, reason: CoinReason, refId: UUID?): Long {
        transactions += Transaction(userId, amount, reason, refId)
        val index = userRepository.users.indexOfFirst { it.id == userId }
        val user = userRepository.users[index]
        userRepository.users[index] = user.copy(coinBalance = user.coinBalance + amount)
        return userRepository.users[index].coinBalance
    }
}
