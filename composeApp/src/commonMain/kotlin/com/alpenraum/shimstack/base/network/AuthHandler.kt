package com.alpenraum.shimstack.base.network

import com.alpenraum.shimstack.BuildKonfig
import com.alpenraum.shimstack.base.logger.ShimstackLogger
import com.alpenraum.shimstack.base.logger.WithLogger
import com.alpenraum.shimstack.base.resolve
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.koin.core.annotation.Single

@Single
class AuthHandler(
    private val authStorage: AuthStorage,
    private val httpClient: HttpClient,
    private val apiResolver: ApiResolver,
    logger: ShimstackLogger
) : WithLogger(logger) {
    suspend fun getAccessTokenFromStorage(): String? = authStorage.getAccessToken()?.token

    suspend fun fetchAccessToken(): String =
        try {
            val token: ShimstackJwt =
                httpClient
                    .post("/auth") {
                        setBody(BuildKonfig.authSecret)
                    }.resolve(apiResolver)

            authStorage.setAccessToken(token)

            token.token
        } catch (e: ApiError) {
            logger.e("Error while trying to fetch Access token!", e)
            ""
        }
}

class ShimstackJwt(
    val token: String
)