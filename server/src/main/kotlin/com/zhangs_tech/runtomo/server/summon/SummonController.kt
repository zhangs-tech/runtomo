package com.zhangs_tech.runtomo.server.summon

import com.zhangs_tech.runtomo.server.auth.CurrentUserId
import com.zhangs_tech.runtomo.server.summon.dto.BannerResponse
import com.zhangs_tech.runtomo.server.summon.dto.SummonRequest
import com.zhangs_tech.runtomo.server.summon.dto.SummonResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class SummonController(private val summonService: SummonService) {
    @GetMapping("/banners/{code}")
    fun getBanner(@CurrentUserId userId: UUID, @PathVariable code: String): BannerResponse =
        BannerResponse.from(summonService.bannerView(userId, code))

    @PostMapping("/summons")
    @ResponseStatus(HttpStatus.CREATED)
    fun summon(@CurrentUserId userId: UUID, @RequestBody request: SummonRequest): SummonResponse =
        SummonResponse.from(summonService.summon(userId, request.bannerCode))

    @GetMapping("/summons")
    fun getLatest(@CurrentUserId userId: UUID, @RequestParam(defaultValue = "1") limit: Int): List<SummonResponse> =
        summonService.latest(userId, limit).map(SummonResponse::from)
}
