package com.zhangs_tech.runtomo.server.summon

import com.zhangs_tech.runtomo.server.coin.CoinReason
import com.zhangs_tech.runtomo.server.coin.CoinRepository
import com.zhangs_tech.runtomo.server.coin.NotEnoughCoinsException
import com.zhangs_tech.runtomo.server.game.Banner
import com.zhangs_tech.runtomo.server.game.GameCalendar
import com.zhangs_tech.runtomo.server.game.GameData
import com.zhangs_tech.runtomo.server.game.RewardRules
import com.zhangs_tech.runtomo.server.game.Species
import com.zhangs_tech.runtomo.server.tomo.Tomo
import com.zhangs_tech.runtomo.server.tomo.TomoRepository
import com.zhangs_tech.runtomo.server.user.UserNotFoundException
import com.zhangs_tech.runtomo.server.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

data class BannerView(
    val banner: Banner,
    val pool: List<WeightedSpecies>,
    val coinBalance: Long,
    /** Summons until Epic or higher is certain, or null if the pool has no Epic or higher species. */
    val pityRemaining: Int?,
)

data class SummonResult(
    val summon: Summon,
    val species: Species,
    /** The new Tomo, or null for a duplicate. */
    val tomo: Tomo?,
    val duplicateCoins: Int,
    val coinBalance: Long,
)

/** UC-12 Summon Tomo. */
@Service
class SummonService(
    private val gameData: GameData,
    private val gameCalendar: GameCalendar,
    private val roller: SummonRoller,
    private val rewardRules: RewardRules,
    private val userRepository: UserRepository,
    private val tomoRepository: TomoRepository,
    private val coinRepository: CoinRepository,
    private val summonRepository: SummonRepository,
    private val pityRepository: PityRepository,
) {
    fun bannerView(userId: UUID, bannerCode: String): BannerView {
        val banner = gameData.findBanner(bannerCode) ?: throw BannerNotFoundException()
        val user = userRepository.findById(userId) ?: throw UserNotFoundException()
        val pool = poolOf(banner)
        val pityRemaining = if (pool.any { it.species.rarity.isEpicOrHigher }) {
            banner.pityThreshold - pityRepository.pullsSinceEpic(userId, banner.code)
        } else {
            null
        }
        return BannerView(banner, pool, user.coinBalance, pityRemaining)
    }

    /** BR-7: one transaction, so the Player is charged only together with a saved result. */
    @Transactional
    fun summon(userId: UUID, bannerCode: String): SummonResult {
        val banner = gameData.findBanner(bannerCode) ?: throw BannerNotFoundException()
        // The row lock keeps two summons at once from spending the same coins.
        val user = userRepository.lockById(userId) ?: throw UserNotFoundException()
        if (user.coinBalance < banner.cost) {
            val shortfall = banner.cost - user.coinBalance
            throw NotEnoughCoinsException(shortfall, rewardRules.distanceToEarnM(shortfall))
        }

        val pool = poolOf(banner)
        val highPool = pool.filter { it.species.rarity.isEpicOrHigher }
        val pulls = pityRepository.pullsSinceEpic(userId, banner.code)
        // BR-3: after pityThreshold - 1 summons without Epic or higher, the next one is Epic or higher.
        val wasPity = highPool.isNotEmpty() && pulls + 1 >= banner.pityThreshold
        val species = roller.roll(if (wasPity) highPool else pool)
        pityRepository.save(userId, banner.code, if (species.rarity.isEpicOrHigher) 0 else pulls + 1)

        // BR-4: a Player owns at most one Tomo of each species.
        val isDuplicate = tomoRepository.findByUserId(userId).any { it.speciesCode == species.code }
        val tomo = if (isDuplicate) null else tomoRepository.create(userId, species.code, species.name, gameCalendar.today())
        val summon = summonRepository.create(
            userId = userId,
            bannerCode = banner.code,
            speciesCode = species.code,
            rarity = species.rarity,
            costCoins = banner.cost,
            wasPity = wasPity,
            userTomoId = tomo?.id,
        )
        var balance = coinRepository.record(userId, -banner.cost, CoinReason.SUMMON, summon.id)
        if (isDuplicate) balance = coinRepository.record(userId, banner.duplicateCoins, CoinReason.DUPLICATE, summon.id)

        return SummonResult(summon, species, tomo, if (isDuplicate) banner.duplicateCoins else 0, balance)
    }

    /** 6a: after a lost response, the app reads the real result instead of summoning again. */
    fun latest(userId: UUID, limit: Int): List<SummonResult> {
        val user = userRepository.findById(userId) ?: throw UserNotFoundException()
        return summonRepository.findLatest(userId, limit.coerceIn(1, MAX_LATEST)).map { summon ->
            SummonResult(
                summon = summon,
                species = gameData.findSpecies(summon.speciesCode)!!,
                tomo = summon.userTomoId?.let(tomoRepository::findById),
                duplicateCoins = if (summon.wasDuplicate) gameData.findBanner(summon.bannerCode)?.duplicateCoins ?: 0 else 0,
                coinBalance = user.coinBalance,
            )
        }
    }

    private fun poolOf(banner: Banner): List<WeightedSpecies> =
        banner.species.map { WeightedSpecies(gameData.findSpecies(it.code)!!, it.weight) }

    private companion object {
        const val MAX_LATEST = 20
    }
}
