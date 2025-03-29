package com.alpenraum.shimstack.base.network

import com.alpenraum.shimstack.BuildKonfig
import com.alpenraum.shimstack.base.BuildInfo
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.HttpHeaders
import io.ktor.http.userAgent
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object HttpClientProvider : KoinComponent {
    val client =
        HttpClient {
            installDefaultFeatures()
        }

    private fun HttpClientConfig<*>.installDefaultFeatures() {
        if (BuildInfo.isDebug()) {
            install(Logging) {
                level = LogLevel.ALL
                sanitizeHeader { header -> header == HttpHeaders.Authorization }
            }
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 30_000
            socketTimeoutMillis = 30_000
        }
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }
            )
        }
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 3)
            exponentialDelay()
        }

        defaultRequest {
            userAgent("shimstack/${BuildInfo.appVersion()}/${BuildInfo.osName()}/${BuildInfo.osVersion()}")
            url(BuildKonfig.baseUrl)
        }

        install(Auth) {
            bearer {
                val tokenHandler = get<AuthHandler>()
                loadTokens {
                    BearerTokens(tokenHandler.getAccessTokenFromStorage() ?: "", null)
                }
                refreshTokens {
                    BearerTokens(tokenHandler.fetchAccessToken(), null)
                }
            }
        }
    }
}