package com.alpenraum.shimstack.domain.bikeservice

import com.alpenraum.shimstack.base.logger.ShimstackLogger
import com.alpenraum.shimstack.base.logger.WithLogger
import com.alpenraum.shimstack.domain.model.bike.Bike
import org.koin.core.annotation.Single

@Single
class UpdateBikeUseCase(
    private val bikeRepository: BikeRepository,
    logger: ShimstackLogger
) : WithLogger(logger) {
    suspend operator fun invoke(bike: Bike): Boolean {
        return try {
            val id = bike.id ?: return false
            val currentBike = bikeRepository.getBike(id) ?: return false
            val update =
                currentBike.copy(
                    name = bike.name,
                    type = bike.type,
                    frontTire = bike.frontTire,
                    rearTire = bike.rearTire,
                    frontSuspension = bike.frontSuspension,
                    rearSuspension = bike.rearSuspension
                )

            bikeRepository.updateBike(update)

            true
        } catch (e: Exception) {
            logger.e("UpdateBikeUseCase", e)
            false
        }
    }
}