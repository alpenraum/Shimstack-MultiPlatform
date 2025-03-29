package com.alpenraum.shimstack.base.network

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import org.koin.core.annotation.Single

@Single
class ApiResolver {
    suspend inline fun <reified T> HttpResponse.handle(): T {
        if (status.value in 200..299) {
            println("Successful response!")
            return body()
        } else {
            throw ApiError(bodyAsText(), ErrorReason.ofCode(status.value))
        }
    }
}

class ApiError(
    val error: String,
    val errorReason: ErrorReason
) : Exception(error)

enum class ErrorReason(
    val httpCode: HttpStatusCode
) {
    BAD_REQUEST(HttpStatusCode.BadRequest), // 400
    UNAUTHORIZED(HttpStatusCode.Unauthorized), // 401
    FORBIDDEN(HttpStatusCode.Forbidden), // 403
    NOT_FOUND(HttpStatusCode.NotFound), // 404
    SESSION_ALREADY_CONNECTED(HttpStatusCode.Locked), // 423
    INTERNAL_SERVER_ERROR(HttpStatusCode.InternalServerError), // 500
    SERVICE_UNAVAILABLE(HttpStatusCode.ServiceUnavailable), // 503
    UNKNOWN(HttpStatusCode(999, "Unknown"))
    ;

    companion object {
        fun ofCode(httpStatusCode: Int): ErrorReason =
            ErrorReason.entries.firstOrNull { it.httpCode.value == httpStatusCode } ?: UNKNOWN
    }
}