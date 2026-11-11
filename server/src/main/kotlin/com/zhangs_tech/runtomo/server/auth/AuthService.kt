package com.zhangs_tech.runtomo.server.auth

import com.zhangs_tech.runtomo.server.auth.dto.SignInRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val identityVerifier: IdentityVerifier
) {
    public fun signIn(idToken: String) {
        val identity = identityVerifier.verify(idToken)

    }

    public fun signOut() {}
    public fun refreshToken() {}
}