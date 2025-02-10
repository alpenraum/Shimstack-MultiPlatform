package com.alpenraum.shimstack.domain.setupwizard

import com.alpenraum.shimstack.domain.SetupRecommendationRepository
import com.alpenraum.shimstack.domain.model.bike.Bike
import com.alpenraum.shimstack.domain.setupwizard.symptomsolvers.BottomOutSymptomSolver
import com.alpenraum.shimstack.domain.setupwizard.symptomsolvers.BrakeDiveSymptomSolver
import com.alpenraum.shimstack.domain.setupwizard.symptomsolvers.FrontFlipOnTakeoffSymptomSolver
import com.alpenraum.shimstack.domain.setupwizard.symptomsolvers.MushSymptomSolver
import com.alpenraum.shimstack.domain.setupwizard.symptomsolvers.OversteerSymptomSolver
import com.alpenraum.shimstack.domain.setupwizard.symptomsolvers.RareFullTravelSymptomSolver
import com.alpenraum.shimstack.domain.setupwizard.symptomsolvers.TooHarshSymptomSolver
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
    private val mushSymptomSolver: MushSymptomSolver,
    private val tooHarshSymptomSolver: TooHarshSymptomSolver,
    private val brakeDiveSymptomSolver: BrakeDiveSymptomSolver,
    private val bottomOutSymptomSolver: BottomOutSymptomSolver,
    private val rareFullTravelSymptomSolver: RareFullTravelSymptomSolver,
    private val frontFlipOnTakeoffSymptomSolver: FrontFlipOnTakeoffSymptomSolver
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
                SetupSymptom.HARSH_OVER_SMALL_BUMPS -> solveTooHarsh(bike, isFront, currentWizardSession)
                SetupSymptom.BRAKE_DIVE ->
                    SetupRecommendation(
                        bikeId = bike.id ?: -1,
                        wizardSession = currentWizardSession,
                        frontSagDelta = brakeDiveSymptomSolver.solve(bike.frontSuspension)
                    )

                SetupSymptom.STEEP_DIVE ->
                    SetupRecommendation(
                        bikeId = bike.id ?: -1,
                        wizardSession = currentWizardSession,
                        frontSagDelta = brakeDiveSymptomSolver.solve(bike.frontSuspension)
                    )

                SetupSymptom.FREQUENT_BOTTOM_OUT -> solveFrequentBottomOut(bike, isFront, currentWizardSession)
                SetupSymptom.RARE_FULL_TRAVEL -> solveRareFullTravel(bike, isFront, currentWizardSession)
                SetupSymptom.FRONT_FLIP_ON_TAKE_OFF -> solveFrontFlipOnTakeoff(bike, currentWizardSession)
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

    private fun solveTooHarsh(
        bike: Bike,
        isFront: Boolean,
        currentWizardSession: String
    ): SetupRecommendation =
        if (isFront) {
            SetupRecommendation(
                wizardSession = currentWizardSession,
                bikeId = bike.id ?: -1,
                frontSagDelta = tooHarshSymptomSolver.solve(bike.frontSuspension)
            )
        } else {
            SetupRecommendation(
                wizardSession = currentWizardSession,
                bikeId = bike.id ?: -1,
                rearSagDelta = tooHarshSymptomSolver.solve(bike.rearSuspension)
            )
        }

    private fun solveFrequentBottomOut(
        bike: Bike,
        isFront: Boolean,
        currentWizardSession: String
    ): SetupRecommendation =
        if (isFront) {
            val result = bottomOutSymptomSolver.solve(bike.frontSuspension)
            if (result.sagDelta != null) {
                SetupRecommendation(
                    wizardSession = currentWizardSession,
                    bikeId = bike.id ?: -1,
                    frontSagDelta = result.sagDelta
                )
            } else {
                SetupRecommendation(
                    wizardSession = currentWizardSession,
                    bikeId = bike.id ?: -1,
                    frontTokenDelta = result.tokenDelta
                )
            }
        } else {
            val result = bottomOutSymptomSolver.solve(bike.rearSuspension)
            if (result.sagDelta != null) {
                SetupRecommendation(
                    wizardSession = currentWizardSession,
                    bikeId = bike.id ?: -1,
                    frontSagDelta = result.sagDelta
                )
            } else {
                SetupRecommendation(
                    wizardSession = currentWizardSession,
                    bikeId = bike.id ?: -1,
                    frontTokenDelta = result.tokenDelta
                )
            }
        }

    private fun solveRareFullTravel(
        bike: Bike,
        isFront: Boolean,
        currentWizardSession: String
    ): SetupRecommendation =
        if (isFront) {
            val result = rareFullTravelSymptomSolver.solve(bike.frontSuspension)
            if (result.sagDelta != null) {
                SetupRecommendation(
                    wizardSession = currentWizardSession,
                    bikeId = bike.id ?: -1,
                    frontSagDelta = result.sagDelta
                )
            } else {
                SetupRecommendation(
                    wizardSession = currentWizardSession,
                    bikeId = bike.id ?: -1,
                    frontTokenDelta = result.tokenDelta
                )
            }
        } else {
            val result = rareFullTravelSymptomSolver.solve(bike.rearSuspension)
            if (result.sagDelta != null) {
                SetupRecommendation(
                    wizardSession = currentWizardSession,
                    bikeId = bike.id ?: -1,
                    frontSagDelta = result.sagDelta
                )
            } else {
                SetupRecommendation(
                    wizardSession = currentWizardSession,
                    bikeId = bike.id ?: -1,
                    frontTokenDelta = result.tokenDelta
                )
            }
        }

    private fun solveFrontFlipOnTakeoff(
        bike: Bike,
        currentWizardSession: String
    ): SetupRecommendation {
        val result = frontFlipOnTakeoffSymptomSolver.solve(bike)

        return SetupRecommendation(
            bikeId = bike.id ?: -1,
            wizardSession = currentWizardSession,
            frontLSRDelta = result.frontLSRDelta,
            rearLSRDelta = result.rearLSRDelta
        )
    }
}