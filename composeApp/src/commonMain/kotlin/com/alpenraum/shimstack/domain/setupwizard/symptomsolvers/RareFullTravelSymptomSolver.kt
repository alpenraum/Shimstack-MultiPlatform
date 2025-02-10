package com.alpenraum.shimstack.domain.setupwizard.symptomsolvers

import com.alpenraum.shimstack.domain.model.suspension.Suspension
import org.koin.core.annotation.Single

@Single
class RareFullTravelSymptomSolver(
    private val harshSymptomSolver: TooHarshSymptomSolver
) : SymptomSolver<Suspension?, RareFullTravelSolution> {
    override fun solve(input: Suspension?): RareFullTravelSolution =
        if ((input?.sag ?: 0.0) <= 0.2) {
            RareFullTravelSolution(sagDelta = harshSymptomSolver.solve(input))
        } else {
            RareFullTravelSolution(tokenDelta = -1)
        }
}

data class RareFullTravelSolution(
    val sagDelta: Double? = null,
    val tokenDelta: Int? = null
)