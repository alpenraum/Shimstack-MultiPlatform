package com.alpenraum.shimstack.base.network

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single

@Single
class AuthStorage {
    private var accessToken: ShimstackJwt? = null

    private val accessTokenMutex = Mutex()

    suspend fun setAccessToken(accessToken: ShimstackJwt) =
        accessTokenMutex.withLock {
            this.accessToken = accessToken
        }

    suspend fun getAccessToken(): ShimstackJwt? =
        accessTokenMutex.withLock {
            return@withLock this.accessToken
        }
}