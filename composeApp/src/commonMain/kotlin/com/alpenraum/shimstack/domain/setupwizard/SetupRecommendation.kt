package com.alpenraum.shimstack.domain.setupwizard

import com.alpenraum.shimstack.data.model.setupRecommendation.SetupRecommendationDTO

class SetupRecommendation(
    val id: Int? = null,
    val bikeId: Int,
    val wizardSession: String,
    val frontTirePressureDelta: Double? = null,
    val rearTirePressureDelta: Double? = null,
    val frontSagDelta: Double? = null,
    val rearSagDelta: Double? = null,
    val frontHSCDelta: Int? = null,
    val rearHSCDelta: Int? = null,
    val frontLSCDelta: Int? = null,
    val rearLSCDelta: Int? = null,
    val frontHSRDelta: Int? = null,
    val rearHSRDelta: Int? = null,
    val frontLSRDelta: Int? = null,
    val rearLSRDelta: Int? = null,
    val frontTokenDelta: Int? = null,
    val rearTokenDelta: Int? = null,
    val isAccepted: Boolean? = null
) {
    fun toDto() =
        SetupRecommendationDTO(
            id = id,
            bikeId = bikeId,
            wizardSession = wizardSession,
            frontTirePressureDelta = frontTirePressureDelta,
            rearTirePressureDelta = rearTirePressureDelta,
            frontSagDelta = frontSagDelta,
            rearSagDelta = rearSagDelta,
            frontHSCDelta = frontHSCDelta,
            rearHSCDelta = rearHSCDelta,
            frontLSCDelta = frontLSCDelta,
            rearLSCDelta = rearLSCDelta,
            frontHSRDelta = frontHSRDelta,
            rearHSRDelta = rearHSRDelta,
            frontLSRDelta = frontLSRDelta,
            rearLSRDelta = rearLSRDelta,
            frontTokenDelta = frontTokenDelta,
            rearTokenDelta = rearTokenDelta,
            isAccepted = isAccepted
        )
}

class NoFittingSolutionException(
    message: String
) : Exception(message)