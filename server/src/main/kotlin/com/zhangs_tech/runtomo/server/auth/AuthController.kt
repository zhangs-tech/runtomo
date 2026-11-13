package com.zhangs_tech.runtomo.server.auth

import com.zhangs_tech.runtomo.server.auth.dto.RefreshTokenRequest
import com.zhangs_tech.runtomo.server.auth.dto.RefreshTokenResponse
import com.zhangs_tech.runtomo.server.auth.dto.SignInRequest
import com.zhangs_tech.runtomo.server.auth.dto.SignInResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {
    @PostMapping("/signin")
    fun signIn(@Valid @RequestBody signInRequest: SignInRequest): SignInResponse =
        authService.signIn(signInRequest.idToken)

    @PostMapping("/refresh")
    fun refreshToken(@Valid @RequestBody refreshTokenRequest: RefreshTokenRequest): RefreshTokenResponse =
        authService.refreshToken(refreshTokenRequest.refreshToken)

    @PostMapping("/signout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun signOut(@Valid @RequestBody refreshTokenRequest: RefreshTokenRequest) =
        authService.signOut(refreshTokenRequest.refreshToken)
}
