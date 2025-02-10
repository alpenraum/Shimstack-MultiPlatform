package com.alpenraum.shimstack.domain.setupwizard.symptomsolvers

import com.alpenraum.shimstack.domain.model.bike.Bike
import org.koin.core.annotation.Single

@Single
class FrontFlipOnTakeoffSymptomSolver : SymptomSolver<Bike, FrontFlipOnTakeoffSolution> {
    override fun solve(input: Bike): FrontFlipOnTakeoffSolution {
        val frontDelta =
            with(input.frontSuspension?.rebound?.lowSpeedFromClosed) { if ((this ?: 5) <= 5) this?.plus(2) else null }

        val rearLSRDelta =
            with(input.rearSuspension?.rebound?.lowSpeedFromClosed) { this?.minus(2) }

        return FrontFlipOnTakeoffSolution(frontDelta, rearLSRDelta)
    }
}

data class FrontFlipOnTakeoffSolution(
    val frontLSRDelta: Int? = null,
    val rearLSRDelta: Int? = null
)