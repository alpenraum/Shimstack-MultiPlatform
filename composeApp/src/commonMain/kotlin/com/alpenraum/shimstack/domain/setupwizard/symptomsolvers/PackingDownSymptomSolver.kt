package com.alpenraum.shimstack.domain.setupwizard.symptomsolvers

import com.alpenraum.shimstack.domain.model.suspension.Suspension
import org.koin.core.annotation.Single

@Single
class PackingDownSymptomSolver : SymptomSolver<Pair<Suspension, Boolean>, PackingDownSolution> {
    override fun solve(input: Pair<Suspension, Boolean>): PackingDownSolution =
        if (input.second && input.first.rebound.highSpeedFromClosed != null) {
            val originalValue = input.first.rebound.highSpeedFromClosed!!
            PackingDownSolution(hsrDelta = originalValue + 2)
        } else {
            val originalValue = input.first.rebound.lowSpeedFromClosed
            PackingDownSolution(lsrDelta = originalValue + 2)
        }
}

data class PackingDownSolution(
    val lsrDelta: Int? = null,
    val hsrDelta: Int? = null
)