package com.alpenraum.shimstack.data.ridetracker

import com.alpenraum.shimstack.data.model.ridetracker.GpsPointDto
import com.alpenraum.shimstack.data.model.ridetracker.RideUpdateDto
import com.alpenraum.shimstack.domain.model.ridetracker.GpsPoint
import com.alpenraum.shimstack.domain.model.ridetracker.Ride
import io.ktor.client.HttpClient
import org.koin.core.annotation.Single
import org.koin.core.component.KoinComponent

@Single
class RideTrackerApiRepository(
    private val httpClient: HttpClient
) : KoinComponent,
    RideUpdateConsumer {
    private var socketHandler: WebSocketHandler? = null

    private suspend fun initialiseRemoteSession(rideId: String) {
        socketHandler =
            WebSocketHandler(httpClient).also {
                it.createConnection(rideId)
            }
        socketHandler
    }

    override suspend fun consumeUpdate(
        ride: Ride,
        gpsPoints: List<GpsPoint>
    ) {
        if (ride.remoteId == null) {
            throw IllegalStateException("remoteId for ride may not be null! $ride")
        }
        if (socketHandler == null) {
            initialiseRemoteSession(ride.remoteId)
        }
        val rideUpdateDto =
            RideUpdateDto(
                ride.startTime,
                ride.endTime,
                ride.totalDistance,
                ride.totalElevation,
                ride.averageSpeed,
                ride.topSpeed,
                gpsPoints.map { GpsPointDto.fromDomain(it) }
            )

        socketHandler?.uploadUpdate(rideUpdateDto)
    }

    suspend fun finishRide() {
        socketHandler?.closeConnection()
        socketHandler = null
    }
}