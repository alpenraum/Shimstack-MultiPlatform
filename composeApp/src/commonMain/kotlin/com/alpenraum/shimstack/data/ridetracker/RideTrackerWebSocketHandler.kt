package com.alpenraum.shimstack.data.ridetracker

import com.alpenraum.shimstack.data.model.ridetracker.RideUpdateDto
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.close
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Factory

@Factory
class WebSocketHandler(
    private val httpClient: HttpClient
) {
    private lateinit var webSocket: DefaultClientWebSocketSession

    suspend fun createConnection(rideId: String) {
        webSocket =
            httpClient.webSocketSession("/ws/app/$rideId")
    }

    suspend fun uploadUpdate(rideUpdateDto: RideUpdateDto) {
        webSocket.sendSerialized(rideUpdateDto)
    }

    suspend fun closeConnection() {
        webSocket.sendSerialized(WebSocketDto(WebSocketAction.RIDE_FINISHED))
        webSocket.close()
    }
}

@Serializable
class WebSocketDto(
    val action: WebSocketAction,
    val jsonPayload: String? = null
)

@Serializable
enum class WebSocketAction(
    val debugOnly: Boolean
) {
    RIDE_UPDATE(false),
    RIDE_FINISHED(false),
    RIDE_NOT_YET_STARTED(false),

    // region debug
    GET_DATA(true)

    // endregion
}