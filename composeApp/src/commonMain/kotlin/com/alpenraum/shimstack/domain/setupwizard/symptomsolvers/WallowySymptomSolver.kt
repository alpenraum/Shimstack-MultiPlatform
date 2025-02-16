package com.alpenraum.shimstack.domain.setupwizard.symptomsolvers

import com.alpenraum.shimstack.domain.model.bike.Bike
import org.koin.core.annotation.Single

@Single
class WallowySymptomSolver : SymptomSolver<Bike, Pair<Double, Double>> {
    override fun solve(input: Bike): Pair<Double, Double> =
        input.frontTire.pressure.asMetric() + 0.2 to input.rearTire.pressure.asMetric() + 0.2
}