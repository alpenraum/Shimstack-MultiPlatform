package com.alpenraum.shimstack.domain.setupwizard

import com.alpenraum.shimstack.domain.SetupRecommendationRepository
import com.alpenraum.shimstack.domain.model.bike.Bike
import com.alpenraum.shimstack.domain.setupwizard.symptomsolvers.MushSymptomSolver
import com.alpenraum.shimstack.domain.setupwizard.symptomsolvers.OversteerSymptomSolver
import com.alpenraum.shimstack.domain.setupwizard.symptomsolvers.UndersteerSymptomSolver
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * V1 - hard coded problem solving
 * V2 - when collected enough data, make it based on machine learning categorisation
 */
@Single
class GetSetupSolutionUseCase(
    private val setupRecommendationRepository: SetupRecommendationRepository,
    private val understeerSymptomSolver: UndersteerSymptomSolver,
    private val oversteerSymptomSolver: OversteerSymptomSolver,
    private val mushSymptomSolver: MushSymptomSolver
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(
        issue: SetupSymptom,
        bike: Bike,
        isFront: Boolean,
        isOnHighSpeed: Boolean
    ): SetupRecommendation {
        val currentWizardSession =
            setupRecommendationRepository.getOpenWizardSessionForBike(bikeId = bike.id ?: -1) ?: Uuid.random().toHexString()

        val recommendation =
            when (issue) {
                SetupSymptom.UNDERSTEER ->
                    SetupRecommendation(
                        wizardSession = currentWizardSession,
                        bikeId = bike.id ?: -1,
                        frontTirePressureDelta = understeerSymptomSolver.solve(bike.frontTire)
                    )

                SetupSymptom.OVERSTEER ->
                    SetupRecommendation(
                        wizardSession = currentWizardSession,
                        bikeId = bike.id ?: -1,
                        rearTirePressureDelta = oversteerSymptomSolver.solve(bike.rearTire)
                    )

                SetupSymptom.MUSH -> solveMush(bike, isFront, currentWizardSession)
                SetupSymptom.HARSH_OVER_SMALL_BUMPS -> TODO()
                SetupSymptom.BRAKE_DIVE -> TODO()
                SetupSymptom.STEEP_DIVE -> TODO()
                SetupSymptom.FREQUENT_BOTTOM_OUT -> TODO()
                SetupSymptom.RARE_FULL_TRAVEL -> TODO()
                SetupSymptom.FRONT_FLIP_ON_TAKE_OFF -> TODO()
                SetupSymptom.BIKE_PACKING_DOWN -> TODO()
                SetupSymptom.BIKE_BLOWS_THROUGH_TRAVEL -> TODO()
                SetupSymptom.SLUGGISH_HANDLING -> TODO()
                SetupSymptom.WALLLOWY -> TODO()
                SetupSymptom.BIKE_TOO_MUCH_COMP -> TODO()
            }

        setupRecommendationRepository.saveSetupRecommendation(recommendation)

        return recommendation
    }

    private fun solveMush(
        bike: Bike,
        isFront: Boolean,
        currentWizardSession: String
    ): SetupRecommendation =
        if (isFront) {
            SetupRecommendation(
                wizardSession = currentWizardSession,
                bikeId = bike.id ?: -1,
                frontSagDelta = mushSymptomSolver.solve(bike.frontSuspension)
            )
        } else {
            SetupRecommendation(
                wizardSession = currentWizardSession,
                bikeId = bike.id ?: -1,
                rearSagDelta = mushSymptomSolver.solve(bike.rearSuspension)
            )
        }
}