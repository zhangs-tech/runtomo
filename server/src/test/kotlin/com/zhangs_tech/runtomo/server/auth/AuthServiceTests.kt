package com.zhangs_tech.runtomo.server.auth

import com.zhangs_tech.runtomo.server.user.FriendCodeGenerator
import com.zhangs_tech.runtomo.server.user.InMemoryUserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.oauth2.jwt.Jwt
import java.time.Instant
import java.util.UUID

class AuthServiceTests {

    private val identityVerifier = object : IdentityVerifier {
        override fun verify(idToken: String): Identity = Identity(idToken, "$idToken@example.com", null)
    }

    private val tokenService = TokenService {
        Jwt.withTokenValue("test-access-token").header("alg", "RS256").claim("sub", "test").build()
    }

    private val refreshTokenRepository = InMemoryRefreshTokenRepository()

    private val authService = AuthService(
        identityVerifier,
        InMemoryUserRepository(),
        FriendCodeGenerator(),
        tokenService,
        refreshTokenRepository
    )

    @Test
    fun `sign in generates a new friend code when the first one is taken`() {
        val userRepository = InMemoryUserRepository()
        val codes = ArrayDeque(listOf("00000001", "00000001", "00000002"))
        val friendCodeGenerator = object : FriendCodeGenerator() {
            override fun generate(): String = codes.removeFirst()
        }
        val authService = AuthService(
            identityVerifier,
            userRepository,
            friendCodeGenerator,
            tokenService,
            InMemoryRefreshTokenRepository()
        )

        authService.signIn("sub-1")
        authService.signIn("sub-2")

        assertEquals(listOf("00000001", "00000002"), userRepository.users.map { it.friendCode })
    }

    @Test
    fun `sign in uses Runner plus 4 digits when the Google profile has no name`() {
        val userRepository = InMemoryUserRepository()
        val authService = AuthService(
            identityVerifier,
            userRepository,
            FriendCodeGenerator(),
            tokenService,
            InMemoryRefreshTokenRepository()
        )

        authService.signIn("sub-1")

        assertTrue(userRepository.users.single().displayName.matches(Regex("Runner\\d{4}")))
    }

    @Test
    fun `refresh revokes the old token and issues a new one in the same family`() {
        val oldToken = authService.signIn("sub-1").refreshToken

        val newToken = authService.refreshToken(oldToken).refreshToken

        assertNotEquals(oldToken, newToken)
        val old = findToken(oldToken)
        val new = findToken(newToken)
        assertNotNull(old.revokedAt)
        assertNull(new.revokedAt)
        assertEquals(old.familyId, new.familyId)
    }

    @Test
    fun `refresh with an unknown token fails`() {
        assertThrows<InvalidRefreshTokenException> { authService.refreshToken("unknown") }
    }

    @Test
    fun `refresh with an expired token fails`() {
        val token = "expired-token"
        refreshTokenRepository.createFamily(
            userId = UUID.randomUUID(),
            tokenHash = tokenService.hashRefreshToken(token),
            expiresAt = Instant.now().minusSeconds(1)
        )

        assertThrows<InvalidRefreshTokenException> { authService.refreshToken(token) }
    }

    @Test
    fun `refresh with a revoked token revokes the whole family`() {
        val oldToken = authService.signIn("sub-1").refreshToken
        authService.refreshToken(oldToken)

        assertThrows<InvalidRefreshTokenException> { authService.refreshToken(oldToken) }

        val familyId = findToken(oldToken).familyId
        assertTrue(refreshTokenRepository.tokens.filter { it.familyId == familyId }.all { it.revokedAt != null })
    }

    @Test
    fun `sign out revokes the refresh token`() {
        val token = authService.signIn("sub-1").refreshToken

        authService.signOut(token)

        assertNotNull(findToken(token).revokedAt)
        assertThrows<InvalidRefreshTokenException> { authService.refreshToken(token) }
    }

    @Test
    fun `sign out with an unknown token does nothing`() {
        authService.signOut("unknown")
    }

    private fun findToken(token: String): RefreshToken =
        refreshTokenRepository.findByTokenHash(tokenService.hashRefreshToken(token))!!
}
