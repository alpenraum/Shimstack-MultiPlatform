package com.alpenraum.shimstack.domain.setupwizard.symptomsolvers

import com.alpenraum.shimstack.domain.model.suspension.Suspension
import org.koin.core.annotation.Single

@Single
class BlowingThroughSymptomSolver : SymptomSolver<Pair<Suspension, Boolean>, BlowingThroughSolution> {
    override fun solve(input: Pair<Suspension, Boolean>): BlowingThroughSolution =
        if (input.second && input.first.compression.highSpeedFromClosed != null) {
            val originalValue = input.first.compression.highSpeedFromClosed!!
            BlowingThroughSolution(hscDelta = originalValue + 2)
        } else {
            val originalValue = input.first.compression.lowSpeedFromClosed
            BlowingThroughSolution(lscDelta = originalValue + 2)
        }
}

data class BlowingThroughSolution(
    val lscDelta: Int? = null,
    val hscDelta: Int? = null
)