package com.alpenraum.shimstack.domain.setupwizard.symptomsolvers

import com.alpenraum.shimstack.domain.model.bike.Bike
import org.koin.core.annotation.Single

@Single
class UncomfortableSymptomSolver : SymptomSolver<UncomfortableSymptomInput, UncomfortableSymptomResult> {
    override fun solve(input: UncomfortableSymptomInput): UncomfortableSymptomResult {
        val sag = if (input.isFront) input.bike.frontSuspension?.sag ?: 0.0 else input.bike.rearSuspension?.sag ?: 0.0
        val lsc =
            if (input.isFront) {
                input.bike.frontSuspension
                    ?.compression
                    ?.lowSpeedFromClosed
                    ?: 0
            } else {
                input.bike.rearSuspension
                    ?.compression
                    ?.highSpeedFromClosed ?: 0
            }
        val hsc =
            if (input.isFront) {
                input.bike.frontSuspension
                    ?.compression
                    ?.highSpeedFromClosed
                    ?: 0
            } else {
                input.bike.rearSuspension
                    ?.compression
                    ?.highSpeedFromClosed ?: 0
            }

        return if (sag < 0.23) {
            UncomfortableSymptomResult(isFront = input.isFront, sagDelta = sag + 0.03)
        } else if (sag > 0.33) {
            UncomfortableSymptomResult(isFront = input.isFront, sagDelta = sag - 0.03)
        } else {
            if (input.isHighSpeed &&
                input.bike.frontSuspension
                    ?.compression
                    ?.highSpeedFromClosed != null
            ) {
                UncomfortableSymptomResult(isFront = input.isFront, hscDelta = hsc + 2)
            } else {
                UncomfortableSymptomResult(isFront = input.isFront, lscDelta = lsc + 2)
            }
        }
    }
}

data class UncomfortableSymptomInput(
    val bike: Bike,
    val isFront: Boolean,
    val isHighSpeed: Boolean
)

data class UncomfortableSymptomResult(
    val isFront: Boolean,
    val sagDelta: Double? = null,
    val lscDelta: Int? = null,
    val hscDelta: Int? = null
)