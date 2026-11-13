package com.zhangs_tech.runtomo.server.web

import com.zhangs_tech.runtomo.server.activity.DayClosedException
import com.zhangs_tech.runtomo.server.activity.InvalidStepsException
import com.zhangs_tech.runtomo.server.auth.InvalidIdTokenException
import com.zhangs_tech.runtomo.server.auth.InvalidRefreshTokenException
import com.zhangs_tech.runtomo.server.quest.QuestAlreadyClaimedException
import com.zhangs_tech.runtomo.server.quest.QuestNotCompleteException
import com.zhangs_tech.runtomo.server.quest.QuestNotFoundException
import com.zhangs_tech.runtomo.server.summon.BannerNotFoundException
import com.zhangs_tech.runtomo.server.summon.NotEnoughCoinsException
import com.zhangs_tech.runtomo.server.tomo.InvalidNicknameException
import com.zhangs_tech.runtomo.server.tomo.NotAStarterException
import com.zhangs_tech.runtomo.server.tomo.StarterAlreadyChosenException
import com.zhangs_tech.runtomo.server.tomo.TomoNotFoundException
import com.zhangs_tech.runtomo.server.user.InvalidDisplayNameException
import com.zhangs_tech.runtomo.server.user.UserNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.net.URI

// Maps domain exceptions to RFC 9457 problem responses. Services never know about HTTP.
@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(
        InvalidIdTokenException::class,
        InvalidRefreshTokenException::class,
        UserNotFoundException::class,
    )
    fun handleUnauthorized(e: RuntimeException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.message)

    @ExceptionHandler(
        InvalidDisplayNameException::class,
        InvalidNicknameException::class,
        NotAStarterException::class,
        InvalidStepsException::class,
    )
    fun handleBadRequest(e: RuntimeException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message)

    @ExceptionHandler(QuestNotFoundException::class, TomoNotFoundException::class, BannerNotFoundException::class)
    fun handleNotFound(e: RuntimeException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message)

    @ExceptionHandler(DayClosedException::class)
    fun handleDayClosed(e: DayClosedException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.message).apply {
            type = URI.create("/problems/day-closed")
        }

    @ExceptionHandler(NotEnoughCoinsException::class)
    fun handleNotEnoughCoins(e: NotEnoughCoinsException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.message).apply {
            type = URI.create("/problems/not-enough-coins")
            setProperty("shortfall", e.shortfall)
            setProperty("distanceNeededM", e.distanceNeededM)
        }

    @ExceptionHandler(
        StarterAlreadyChosenException::class,
        QuestNotCompleteException::class,
        QuestAlreadyClaimedException::class,
    )
    fun handleConflict(e: RuntimeException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.message)
}
