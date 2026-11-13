package com.zhangs_tech.runtomo.server.auth

import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import java.util.HexFormat
import java.util.UUID

@Service
class TokenService(private val jwtEncoder: JwtEncoder) {
    private val secureRandom = SecureRandom()

    fun issueAccessToken(userId: UUID): String {
        val now = Instant.now()

        val claims = JwtClaimsSet.builder()
            .subject(userId.toString())
            .issuedAt(now)
            .expiresAt(now.plusSeconds(ACCESS_TOKEN_EXPIRY_SECONDS))
            .build()

        val header = JwsHeader.with(SignatureAlgorithm.RS256).build()
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).tokenValue
    }

    fun issueRefreshToken(): Pair<String, Instant> {
        val bytes = ByteArray(REFRESH_TOKEN_BYTE_LENGTH)
        secureRandom.nextBytes(bytes)
        val token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        val expiresAt = Instant.now().plusSeconds(REFRESH_TOKEN_EXPIRY_SECONDS)
        return Pair(token, expiresAt)
    }

    fun hashRefreshToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(token.toByteArray(StandardCharsets.UTF_8))
        return HexFormat.of().formatHex(digest)
    }

    companion object {
        public const val ACCESS_TOKEN_EXPIRY_SECONDS = 15L * 60 // 15 mins
        private const val REFRESH_TOKEN_BYTE_LENGTH = 32
        private const val REFRESH_TOKEN_EXPIRY_SECONDS = 30L * 24 * 60 * 60 // 30 days
    }

}