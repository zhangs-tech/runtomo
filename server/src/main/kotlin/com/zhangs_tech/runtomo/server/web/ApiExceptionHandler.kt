package com.zhangs_tech.runtomo.server.web

import com.zhangs_tech.runtomo.server.auth.InvalidIdTokenException
import com.zhangs_tech.runtomo.server.auth.InvalidRefreshTokenException
import com.zhangs_tech.runtomo.server.tomo.InvalidNicknameException
import com.zhangs_tech.runtomo.server.tomo.NotAStarterException
import com.zhangs_tech.runtomo.server.tomo.StarterAlreadyChosenException
import com.zhangs_tech.runtomo.server.user.InvalidDisplayNameException
import com.zhangs_tech.runtomo.server.user.UserNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

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
    )
    fun handleBadRequest(e: RuntimeException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message)

    @ExceptionHandler(StarterAlreadyChosenException::class)
    fun handleConflict(e: RuntimeException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.message)
}
