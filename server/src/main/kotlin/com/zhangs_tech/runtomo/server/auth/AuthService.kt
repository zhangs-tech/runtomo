package com.zhangs_tech.runtomo.server.auth

import com.zhangs_tech.runtomo.server.auth.dto.RefreshTokenResponse
import com.zhangs_tech.runtomo.server.auth.dto.SignInResponse
import com.zhangs_tech.runtomo.server.user.DisplayName
import com.zhangs_tech.runtomo.server.user.FriendCodeGenerator
import com.zhangs_tech.runtomo.server.user.User
import com.zhangs_tech.runtomo.server.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class AuthService(
    private val identityVerifier: IdentityVerifier,
    private val userRepository: UserRepository,
    private val friendCodeGenerator: FriendCodeGenerator,
    private val tokenService: TokenService,
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    @Transactional
    fun signIn(idToken: String): SignInResponse {
        val identity = identityVerifier.verify(idToken) ?: throw InvalidIdTokenException()
        val existingUser = userRepository.findByAuthSubject(identity.subject)
        val user = existingUser ?: createUser(identity)

        val accessToken = tokenService.issueAccessToken(user.id)
        val (refreshToken, expiresAt) = tokenService.issueRefreshToken()
        val hashedRefreshToken = tokenService.hashRefreshToken(refreshToken)

        refreshTokenRepository.createFamily(
            userId = user.id,
            tokenHash = hashedRefreshToken,
            expiresAt = expiresAt
        )

        return SignInResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessTokenExpiresIn = TokenService.ACCESS_TOKEN_EXPIRY_SECONDS,
            isNewUser = existingUser == null
        )
    }

    @Transactional(noRollbackFor = [InvalidRefreshTokenException::class])
    fun refreshToken(refreshToken: String): RefreshTokenResponse {
        val existingToken = refreshTokenRepository.findByTokenHash(tokenService.hashRefreshToken(refreshToken))
            ?: throw InvalidRefreshTokenException()
        if (existingToken.expiresAt <= Instant.now()) throw InvalidRefreshTokenException()

        // A revoked token sent again means it was stolen, so the whole family is revoked (UC-01).
        if (!refreshTokenRepository.revoke(existingToken.id)) {
            refreshTokenRepository.revokeFamily(existingToken.familyId)
            throw InvalidRefreshTokenException()
        }

        val accessToken = tokenService.issueAccessToken(existingToken.userId)
        val (newRefreshToken, newExpiresAt) = tokenService.issueRefreshToken()

        refreshTokenRepository.create(
            userId = existingToken.userId,
            familyId = existingToken.familyId,
            tokenHash = tokenService.hashRefreshToken(newRefreshToken),
            expiresAt = newExpiresAt
        )

        return RefreshTokenResponse(
            accessToken = accessToken,
            accessTokenExpiresIn = TokenService.ACCESS_TOKEN_EXPIRY_SECONDS,
            refreshToken = newRefreshToken
        )
    }

    fun signOut(refreshToken: String) {
        refreshTokenRepository.findByTokenHash(tokenService.hashRefreshToken(refreshToken))
            ?.let { refreshTokenRepository.revoke(it.id) }
    }

    private fun createUser(identity: Identity): User {
        repeat(MAX_FRIEND_CODE_ATTEMPTS) {
            userRepository.create(
                authSubject = identity.subject,
                displayName = DisplayName.defaultFor(identity.name),
                friendCode = friendCodeGenerator.generate()
            )?.let { return it }
        }
        error("No unique friend code after $MAX_FRIEND_CODE_ATTEMPTS attempts")
    }

    private companion object {
        const val MAX_FRIEND_CODE_ATTEMPTS = 5
    }
}
