package com.alpenraum.shimstack.domain.setupwizard.symptomsolvers

import com.alpenraum.shimstack.domain.model.suspension.Suspension
import org.koin.core.annotation.Single

@Single
class BrakeDiveSymptomSolver : SymptomSolver<Suspension?, Double> {
    override fun solve(input: Suspension?): Double = (input?.sag ?: 0.02) - 0.02
}