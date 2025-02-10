package com.alpenraum.shimstack.domain.setupwizard.symptomsolvers

import com.alpenraum.shimstack.domain.model.suspension.Suspension
import org.koin.core.annotation.Single

@Single
class BottomOutSymptomSolver(
    private val mushSymptomSolver: MushSymptomSolver
) : SymptomSolver<Suspension?, BottomOutSolution> {
    override fun solve(input: Suspension?): BottomOutSolution =
        if ((input?.sag ?: 0.0) >= 0.35) {
            BottomOutSolution(sagDelta = mushSymptomSolver.solve(input))
        } else {
            BottomOutSolution(tokenDelta = 1)
        }
}

data class BottomOutSolution(
    val sagDelta: Double? = null,
    val tokenDelta: Int? = null
)