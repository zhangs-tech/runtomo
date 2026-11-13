package com.zhangs_tech.runtomo.server.user

import com.zhangs_tech.runtomo.server.auth.CurrentUserId
import com.zhangs_tech.runtomo.server.user.dto.MeResponse
import com.zhangs_tech.runtomo.server.user.dto.UpdateMeRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/me")
class MeController(private val userService: UserService) {
    @GetMapping
    fun getMe(@CurrentUserId userId: UUID): MeResponse = MeResponse.from(userService.getUser(userId))

    @PatchMapping
    fun updateMe(@CurrentUserId userId: UUID, @RequestBody request: UpdateMeRequest): MeResponse =
        MeResponse.from(userService.updateDisplayName(userId, request.displayName))
}
