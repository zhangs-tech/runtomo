package com.zhangs_tech.runtomo.social

import com.zhangs_tech.runtomo.tomo.testLuppy
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

fun testPlayer(id: String, name: String) = PlayerCard(id, name, DisplayedTomo("$name's Tomo", testLuppy))

class FakeSocialApi : SocialApi {
    var lookUpResult: LookUpResult = LookUpResult.Found(testPlayer("u2", "Ana"))
    var sendResult = SendResult.SENT
    var acceptResult = true
    var error: Exception? = null
    var requests = mutableListOf(IncomingRequest("r1", testPlayer("u3", "Bo")))
    var friendList = mutableListOf(Friend(testPlayer("u2", "Ana"), 1000, 3000, 9000))
    val leaderboardPeriods = mutableListOf<LeaderboardPeriod>()
    var board = Leaderboard(
        rows = listOf(
            LeaderboardRow(1, testPlayer("u2", "Ana"), 3000, isMe = false),
            LeaderboardRow(2, testPlayer("u1", "Kai"), 1200, isMe = true),
        ),
        ahead = testPlayer("u2", "Ana"),
        gapM = 1800,
    )

    override suspend fun lookUp(friendCode: String): LookUpResult = check { lookUpResult }
    override suspend fun sendRequest(toUserId: String): SendResult = check { sendResult }
    override suspend fun incomingRequests(): List<IncomingRequest> = check { requests.toList() }

    override suspend fun accept(requestId: String): Boolean = check {
        if (acceptResult) {
            val request = requests.first { it.id == requestId }
            requests.remove(request)
            friendList.add(Friend(request.from, 0, 0, 0))
        }
        acceptResult
    }

    override suspend fun decline(requestId: String) = check { requests.removeIf { it.id == requestId }; Unit }
    override suspend fun friends(): List<Friend> = check { friendList.toList() }
    override suspend fun remove(friendUserId: String) = check { friendList.removeIf { it.player.userId == friendUserId }; Unit }

    override suspend fun leaderboard(period: LeaderboardPeriod): Leaderboard = check {
        leaderboardPeriods += period
        board
    }

    private fun <T> check(block: () -> T): T {
        error?.let { throw it }
        return block()
    }
}

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [SocialModule::class])
object FakeSocialModule {
    @Provides
    @Singleton
    fun provideSocialApi(): SocialApi = FakeSocialApi()
}
