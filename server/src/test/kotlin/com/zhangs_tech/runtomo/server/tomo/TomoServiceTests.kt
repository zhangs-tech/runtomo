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
}
