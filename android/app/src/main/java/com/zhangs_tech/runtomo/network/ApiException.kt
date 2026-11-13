package com.zhangs_tech.runtomo.network

sealed class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class Unauthorized : ApiException("The server rejected the credentials")
    class Conflict(cause: Throwable) : ApiException("The request conflicts with the current state", cause)
    class Network(cause: Throwable) : ApiException("The server could not be reached", cause)
    class Failed(cause: Throwable) : ApiException("The request failed", cause)
}
