package com.alpenraum.shimstack.domain.setupwizard.symptomsolvers

import com.alpenraum.shimstack.domain.model.tire.Tire
import com.alpenraum.shimstack.domain.setupwizard.tire.CalculateTirePressureOffsetForSymptomUseCase
import org.koin.core.annotation.Single

@Single
class SluggishHandlingSymptomSolver(
) : SymptomSolver<Tire, Double> {
    override fun solve(input: Tire): Double = input.pressure.asMetric() + 0.2
}