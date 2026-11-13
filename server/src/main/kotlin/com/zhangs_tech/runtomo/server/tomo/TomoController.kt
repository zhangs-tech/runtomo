package com.zhangs_tech.runtomo.server.tomo

import com.zhangs_tech.runtomo.server.auth.CurrentUserId
import com.zhangs_tech.runtomo.server.game.GameData
import com.zhangs_tech.runtomo.server.tomo.dto.ChooseStarterRequest
import com.zhangs_tech.runtomo.server.tomo.dto.SpeciesResponse
import com.zhangs_tech.runtomo.server.tomo.dto.TomoResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class TomoController(private val tomoService: TomoService, private val gameData: GameData) {
    @GetMapping("/species/starters")
    fun getStarters(): List<SpeciesResponse> = gameData.starters.map(SpeciesResponse::from)

    @PostMapping("/me/tomos/starter")
    @ResponseStatus(HttpStatus.CREATED)
    fun chooseStarter(@CurrentUserId userId: UUID, @RequestBody request: ChooseStarterRequest): TomoResponse {
        val tomo = tomoService.chooseStarter(userId, request.speciesCode, request.nickname)
        return TomoResponse.from(tomo, gameData.findSpecies(tomo.speciesCode)!!)
    }
}
