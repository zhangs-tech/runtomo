package com.zhangs_tech.runtomo.server.summon

import com.zhangs_tech.runtomo.server.game.Species
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.random.RandomGenerator

data class WeightedSpecies(val species: Species, val weight: Int)

/** Picks a species by integer weight, so the roll needs no floating point (UC-12). */
@Component
class SummonRoller(private val random: RandomGenerator = SecureRandom()) {
    fun roll(pool: List<WeightedSpecies>): Species {
        var remaining = random.nextInt(pool.sumOf { it.weight })
        for (entry in pool) {
            if (remaining < entry.weight) return entry.species
            remaining -= entry.weight
        }
        error("The roll is always below the total weight")
    }
}
