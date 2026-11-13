package com.zhangs_tech.runtomo.server.tomo

import com.zhangs_tech.runtomo.server.game.GameCalendar
import com.zhangs_tech.runtomo.server.game.testClock
import com.zhangs_tech.runtomo.server.game.testGameData
import com.zhangs_tech.runtomo.server.user.InMemoryUserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class TomoServiceTests {
    private val userRepository = InMemoryUserRepository()
    private val tomoRepository = InMemoryTomoRepository()
    private val tomoService = TomoService(testGameData, GameCalendar(testClock), userRepository, tomoRepository)
    private val user = userRepository.create("sub-1", "Runner0001", "00000001")!!

    @Test
    fun `choose starter creates an equipped level 1 Tomo born today`() {
        val tomo = tomoService.chooseStarter(user.id, "luppy", "Mochi")

        assertEquals("Mochi", tomo.nickname)
        assertEquals(1, tomo.bondLevel)
        assertEquals(0, tomo.distanceTogetherM)
        assertEquals(LocalDate.of(2026, 11, 14), tomo.birthday)
        assertEquals(tomo.id, userRepository.findById(user.id)!!.equippedTomoId)
    }

    @Test
    fun `choose starter fails when the Player already owns a Tomo`() {
        tomoService.chooseStarter(user.id, "luppy", "Mochi")

        assertThrows<StarterAlreadyChosenException> { tomoService.chooseStarter(user.id, "rocky", "Rocky") }
        assertEquals(1, tomoRepository.tomos.size)
    }

    @Test
    fun `choose starter rejects a species that is not a starter`() {
        assertThrows<NotAStarterException> { tomoService.chooseStarter(user.id, "nova", "Nova") }
        assertThrows<NotAStarterException> { tomoService.chooseStarter(user.id, "unknown", "X") }
    }

    @Test
    fun `choose starter rejects a nickname that is empty or too long`() {
        assertThrows<InvalidNicknameException> { tomoService.chooseStarter(user.id, "luppy", "") }
        assertThrows<InvalidNicknameException> { tomoService.chooseStarter(user.id, "luppy", "a".repeat(17)) }
    }

    @Test
    fun `collection lists every species and marks the owned ones`() {
        val tomo = tomoService.chooseStarter(user.id, "luppy", "Mochi")

        val collection = tomoService.collection(user.id)

        assertEquals(listOf("luppy", "rocky", "nova"), collection.entries.map { it.species.code })
        assertEquals(tomo, collection.entries[0].tomo)
        assertEquals(null, collection.entries[1].tomo)
        assertEquals(tomo.id, collection.equippedTomoId)
    }

    @Test
    fun `rename saves a valid nickname and rejects an invalid one`() {
        val tomo = tomoService.chooseStarter(user.id, "luppy", "Mochi")

        assertEquals("Kiki", tomoService.rename(user.id, tomo.id, "Kiki").nickname)
        assertThrows<InvalidNicknameException> { tomoService.rename(user.id, tomo.id, "") }
    }

    @Test
    fun `equip sets the equipped Tomo, but only one the Player owns`() {
        tomoService.chooseStarter(user.id, "luppy", "Mochi")
        val second = tomoRepository.create(user.id, "rocky", "Rocky", LocalDate.of(2026, 11, 14))
        val other = userRepository.create("sub-2", "Runner0002", "00000002")!!
        val othersTomo = tomoRepository.create(other.id, "rocky", "Rocky", LocalDate.of(2026, 11, 14))

        tomoService.equip(user.id, second.id)

        assertEquals(second.id, userRepository.findById(user.id)!!.equippedTomoId)
        assertThrows<TomoNotFoundException> { tomoService.equip(user.id, othersTomo.id) }
        assertThrows<TomoNotFoundException> { tomoService.rename(user.id, othersTomo.id, "Mine") }
    }
}
