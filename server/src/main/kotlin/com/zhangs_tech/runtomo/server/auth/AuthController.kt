package com.zhangs_tech.runtomo.server.auth

import com.zhangs_tech.runtomo.server.auth.dto.SignInRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {
    @PostMapping("/signin")
    fun signIn(@RequestBody signInRequest: SignInRequest) {
        authService.signIn(signInRequest.idToken)
    }
}