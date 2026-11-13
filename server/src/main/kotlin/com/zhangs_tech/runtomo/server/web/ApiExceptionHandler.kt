package com.zhangs_tech.runtomo.server.web

import com.zhangs_tech.runtomo.server.auth.InvalidIdTokenException
import com.zhangs_tech.runtomo.server.auth.InvalidRefreshTokenException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

// Maps domain exceptions to RFC 9457 problem responses. Services never know about HTTP.
@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(InvalidIdTokenException::class)
    fun handleInvalidIdToken(e: InvalidIdTokenException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.message)

    @ExceptionHandler(InvalidRefreshTokenException::class)
    fun handleInvalidRefreshToken(e: InvalidRefreshTokenException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.message)
}
