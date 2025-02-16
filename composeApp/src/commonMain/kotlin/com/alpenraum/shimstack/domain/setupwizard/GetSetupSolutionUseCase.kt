package com.alpenraum.shimstack.domain.setupwizard

import com.alpenraum.shimstack.domain.SetupRecommendationRepository
import com.alpenraum.shimstack.domain.model.bike.Bike
import com.alpenraum.shimstack.domain.setupwizard.symptomsolvers.BlowingThroughSymptomSolver
import com.alpenraum.shimstack.domain.setupwizard.symptomsolvers.BottomOutSymptomSolver
import com.alpenraum.shimstack.domain.setupwizard.symptomsolvers.BrakeDiveSymptomSolver
import com.alpenraum.shimstack.domain.setupwizard.symptomsolvers.FrontFlipOnTakeoffSymptomSolver
import com.alpenraum.shimstack.domain.setupwizard.symptomsolvers.MushSymptomSolver
import com.alpenraum.shimstack.domain.setupwizard.symptomsolvers.OversteerSymptomSolver
import com.alpenraum.shimstack.domain.setupwizard.symptomsolvers.PackingDownSymptomSolver
import com.alpenraum.shimstack.domain.setupwizard.symptomsolvers.RareFullTravelSymptomSolver
import com.alpenraum.shimstack.domain.setupwizard.symptomsolvers.SluggishHandlingSymptomSolver
import com.alpenraum.shimstack.domain.setupwizard.symptomsolvers.TooHarshSymptomSolver
import com.alpenraum.shimstack.domain.setupwizard.symptomsolvers.UncomfortableSymptomInput
import com.alpenraum.shimstack.domain.setupwizard.symptomsolvers.UncomfortableSymptomSolver
import com.alpenraum.shimstack.domain.setupwizard.symptomsolvers.UndersteerSymptomSolver
import com.alpenraum.shimstack.domain.setupwizard.symptomsolvers.WallowySymptomSolver
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
    private val frontFlipOnTakeoffSymptomSolver: FrontFlipOnTakeoffSymptomSolver,
    private val bikePackingDownSymptomSolver: PackingDownSymptomSolver,
    private val blowingThroughSymptomSolver: BlowingThroughSymptomSolver,
    private val sluggishHandlingSymptomSolver: SluggishHandlingSymptomSolver,
    private val wallowySymptomSolver: WallowySymptomSolver,
    private val uncomfortableSymptomSolver: UncomfortableSymptomSolver
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
                SetupSymptom.BIKE_PACKING_DOWN -> solveBikePackingDown(bike, isFront, isOnHighSpeed, currentWizardSession)
                SetupSymptom.BIKE_BLOWS_THROUGH_TRAVEL ->
                    solveBikeBlowingThrough(
                        bike,
                        isFront,
                        isOnHighSpeed,
                        currentWizardSession
                    )

                SetupSymptom.SLUGGISH_HANDLING -> solveSluggishHandling(bike, isFront, currentWizardSession)
                SetupSymptom.WALLLOWY -> solveWallowy(bike, currentWizardSession)
                SetupSymptom.BIKE_UNCOMFORTABLE -> solveUncomfortable(bike, isFront, isOnHighSpeed, currentWizardSession)
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
            assertFrontSuspensionExists(bike)
            SetupRecommendation(
                wizardSession = currentWizardSession,
                bikeId = bike.id ?: -1,
                frontSagDelta = mushSymptomSolver.solve(bike.frontSuspension)
            )
        } else {
            assertRearSuspensionExists(bike)
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
            assertFrontSuspensionExists(bike)
            SetupRecommendation(
                wizardSession = currentWizardSession,
                bikeId = bike.id ?: -1,
                frontSagDelta = tooHarshSymptomSolver.solve(bike.frontSuspension)
            )
        } else {
            assertRearSuspensionExists(bike)
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
            assertFrontSuspensionExists(bike)
            val result = bottomOutSymptomSolver.solve(bike.frontSuspension)

            SetupRecommendation(
                wizardSession = currentWizardSession,
                bikeId = bike.id ?: -1,
                frontSagDelta = result.sagDelta,
                frontTokenDelta = result.tokenDelta
            )
        } else {
            assertRearSuspensionExists(bike)
            val result = bottomOutSymptomSolver.solve(bike.rearSuspension)
            SetupRecommendation(
                wizardSession = currentWizardSession,
                bikeId = bike.id ?: -1,
                rearSagDelta = result.sagDelta,
                rearTokenDelta = result.tokenDelta
            )
        }

    private fun solveRareFullTravel(
        bike: Bike,
        isFront: Boolean,
        currentWizardSession: String
    ): SetupRecommendation =
        if (isFront) {
            assertFrontSuspensionExists(bike)
            val result = rareFullTravelSymptomSolver.solve(bike.frontSuspension)

            SetupRecommendation(
                wizardSession = currentWizardSession,
                bikeId = bike.id ?: -1,
                frontSagDelta = result.sagDelta,
                frontTokenDelta = result.tokenDelta
            )
        } else {
            assertRearSuspensionExists(bike)
            val result = rareFullTravelSymptomSolver.solve(bike.rearSuspension)

            SetupRecommendation(
                wizardSession = currentWizardSession,
                bikeId = bike.id ?: -1,
                rearTokenDelta = result.tokenDelta,
                rearSagDelta = result.sagDelta
            )
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

    private fun solveBikePackingDown(
        bike: Bike,
        isFront: Boolean,
        isOnHighSpeed: Boolean,
        currentWizardSession: String
    ): SetupRecommendation {
        val suspension =
            (if (isFront) bike.frontSuspension else bike.rearSuspension)
                ?: throw IllegalArgumentException(
                    "Selected Suspension (${if (isFront) "front" else "rear"} does not exist for this bike! $bike"
                )
        val result = bikePackingDownSymptomSolver.solve(suspension to isOnHighSpeed)

        return if (isFront) {
            SetupRecommendation(
                bikeId = bike.id ?: -1,
                wizardSession = currentWizardSession,
                frontLSRDelta = result.lsrDelta,
                frontHSRDelta = result.hsrDelta
            )
        } else {
            SetupRecommendation(
                bikeId = bike.id ?: -1,
                wizardSession = currentWizardSession,
                rearLSRDelta = result.lsrDelta,
                rearHSRDelta = result.hsrDelta
            )
        }
    }

    private fun solveBikeBlowingThrough(
        bike: Bike,
        isFront: Boolean,
        isOnHighSpeed: Boolean,
        currentWizardSession: String
    ): SetupRecommendation {
        val suspension =
            (if (isFront) bike.frontSuspension else bike.rearSuspension)
                ?: throw IllegalArgumentException(
                    "Selected Suspension (${if (isFront) "front" else "rear"} does not exist for this bike! $bike"
                )
        val result = blowingThroughSymptomSolver.solve(suspension to isOnHighSpeed)

        return if (isFront) {
            SetupRecommendation(
                bikeId = bike.id ?: -1,
                wizardSession = currentWizardSession,
                frontLSCDelta = result.lscDelta,
                frontHSCDelta = result.hscDelta
            )
        } else {
            SetupRecommendation(
                bikeId = bike.id ?: -1,
                wizardSession = currentWizardSession,
                frontHSCDelta = result.hscDelta,
                rearLSCDelta = result.lscDelta
            )
        }
    }

    private fun solveSluggishHandling(
        bike: Bike,
        isFront: Boolean,
        currentWizardSession: String
    ): SetupRecommendation =
        if (isFront) {
            val result = sluggishHandlingSymptomSolver.solve(bike.frontTire)
            SetupRecommendation(
                bikeId = bike.id ?: -1,
                wizardSession = currentWizardSession,
                frontTirePressureDelta = result
            )
        } else {
            val result = sluggishHandlingSymptomSolver.solve(bike.rearTire)
            SetupRecommendation(
                bikeId = bike.id ?: -1,
                wizardSession = currentWizardSession,
                rearTirePressureDelta = result
            )
        }

    private fun solveWallowy(
        bike: Bike,
        currentWizardSession: String
    ): SetupRecommendation {
        val result = wallowySymptomSolver.solve(bike)
        return SetupRecommendation(
            bikeId = bike.id ?: -1,
            wizardSession = currentWizardSession,
            frontTirePressureDelta = result.first,
            rearTirePressureDelta = result.second
        )
    }

    private fun solveUncomfortable(
        bike: Bike,
        isFront: Boolean,
        isOnHighSpeed: Boolean,
        currentWizardSession: String
    ): SetupRecommendation {
        if (isFront) assertFrontSuspensionExists(bike) else assertRearSuspensionExists(bike)
        val result = uncomfortableSymptomSolver.solve(UncomfortableSymptomInput(bike, isFront, isOnHighSpeed))

        val frontSag = if (isFront) result.sagDelta else null
        val rearSag = if (!isFront) result.sagDelta else null

        val frontLscDelta = if (isFront) result.lscDelta else null
        val rearLscDelta = if (!isFront) result.lscDelta else null

        val frontHscDelta = if (isFront) result.hscDelta else null
        val rearHscDelta = if (!isFront) result.hscDelta else null

        return SetupRecommendation(
            bikeId = bike.id ?: -1,
            wizardSession = currentWizardSession,
            frontSagDelta = frontSag,
            rearSagDelta = rearSag,
            frontLSCDelta = frontLscDelta,
            rearLSCDelta = rearLscDelta,
            frontHSCDelta = frontHscDelta,
            rearHSCDelta = rearHscDelta
        )
    }

    private fun assertFrontSuspensionExists(bike: Bike) {
        if (bike.frontSuspension == null) throw NoFittingSolutionException("Bike has no front suspension")
    }

    private fun assertRearSuspensionExists(bike: Bike) {
        if (bike.rearSuspension == null) throw NoFittingSolutionException("Bike has no rear suspension")
    }
}