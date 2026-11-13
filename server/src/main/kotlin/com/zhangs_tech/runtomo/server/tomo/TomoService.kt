package com.zhangs_tech.runtomo.server.tomo

import com.zhangs_tech.runtomo.server.game.GameCalendar
import com.zhangs_tech.runtomo.server.game.GameData
import com.zhangs_tech.runtomo.server.user.UserNotFoundException
import com.zhangs_tech.runtomo.server.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class TomoService(
    private val gameData: GameData,
    private val gameCalendar: GameCalendar,
    private val userRepository: UserRepository,
    private val tomoRepository: TomoRepository,
) {
    @Transactional
    fun chooseStarter(userId: UUID, speciesCode: String, nickname: String): Tomo {
        if (gameData.findSpecies(speciesCode)?.starter != true) throw NotAStarterException()
        if (!Nickname.isValid(nickname)) throw InvalidNicknameException()

        // The row lock stops two requests at once from both creating a starter.
        userRepository.lockById(userId) ?: throw UserNotFoundException()
        if (tomoRepository.findByUserId(userId).isNotEmpty()) throw StarterAlreadyChosenException()

        val tomo = tomoRepository.create(userId, speciesCode, nickname, gameCalendar.today())
        userRepository.setEquippedTomo(userId, tomo.id)
        return tomo
    }
}
