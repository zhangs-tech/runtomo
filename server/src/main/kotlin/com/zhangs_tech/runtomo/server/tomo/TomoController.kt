package com.zhangs_tech.runtomo.server.tomo

import com.zhangs_tech.runtomo.server.auth.CurrentUserId
import com.zhangs_tech.runtomo.server.game.GameData
import com.zhangs_tech.runtomo.server.tomo.dto.ChooseStarterRequest
import com.zhangs_tech.runtomo.server.tomo.dto.CollectionResponse
import com.zhangs_tech.runtomo.server.tomo.dto.EquipTomoRequest
import com.zhangs_tech.runtomo.server.tomo.dto.RenameTomoRequest
import com.zhangs_tech.runtomo.server.tomo.dto.SpeciesResponse
import com.zhangs_tech.runtomo.server.tomo.dto.TomoResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
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

    @GetMapping("/me/tomos")
    fun getCollection(@CurrentUserId userId: UUID): CollectionResponse =
        CollectionResponse.from(tomoService.collection(userId))

    @PatchMapping("/me/tomos/{id}")
    fun rename(
        @CurrentUserId userId: UUID,
        @PathVariable id: UUID,
        @RequestBody request: RenameTomoRequest,
    ): TomoResponse {
        val tomo = tomoService.rename(userId, id, request.nickname)
        return TomoResponse.from(tomo, gameData.findSpecies(tomo.speciesCode)!!)
    }

    @PutMapping("/me/equipped-tomo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun equip(@CurrentUserId userId: UUID, @RequestBody request: EquipTomoRequest) {
        tomoService.equip(userId, request.tomoId)
    }
}
