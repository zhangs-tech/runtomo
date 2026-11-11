package com.zhangs_tech.runtomo.server.auth

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class GoogleIdentityVerifier(
    @Value("\${runtomo.google.web-client-id}") webClientId: String,
) : IdentityVerifier {
    private val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance())
        .setAudience(listOf(webClientId))
        .build()

    override fun verify(idToken: String): Identity? {
        val token = try {
            verifier.verify(idToken) ?: return null // bad signature, expired, wrong aud or iss
        } catch (e: IllegalArgumentException) {
            return null // not a JWT at all
        }

        return Identity(
            subject = token.payload.subject,
            email = token.payload.email,
            name = token.payload["name"] as String?,
        )
    }
}
