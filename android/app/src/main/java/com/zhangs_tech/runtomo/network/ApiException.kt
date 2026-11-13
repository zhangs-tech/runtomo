package com.zhangs_tech.runtomo.network

sealed class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class Unauthorized : ApiException("The server rejected the credentials")
    class NotFound(cause: Throwable) : ApiException("The server found nothing for the request", cause)

    /** [problem] is the server's RFC 9457 body, if it sent one. */
    class Conflict(cause: Throwable, val problem: Problem? = null) :
        ApiException("The request conflicts with the current state", cause) {
        /** For example `/problems/already-friends`. */
        val problemType: String? get() = problem?.type
    }
    class Network(cause: Throwable) : ApiException("The server could not be reached", cause)
    class Failed(cause: Throwable) : ApiException("The request failed", cause)
}
