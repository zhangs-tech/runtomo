package com.zhangs_tech.runtomo.server.auth.dto

import jakarta.validation.constraints.NotBlank


data class SignInRequest(
    @NotBlank
    val idToken: String
)
