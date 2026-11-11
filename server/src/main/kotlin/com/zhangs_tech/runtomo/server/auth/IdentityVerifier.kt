package com.zhangs_tech.runtomo.server.auth

interface IdentityVerifier {
    fun verify(idToken: String): Identity?
}