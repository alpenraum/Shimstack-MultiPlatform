package com.alpenraum.shimstack.ui.setupTroubleshooting

import com.alpenraum.shimstack.domain.setupwizard.SetupSymptom
import org.jetbrains.compose.resources.StringResource
import org.koin.core.annotation.Single
import shimstackmultiplatform.composeapp.generated.resources.Res
import shimstackmultiplatform.composeapp.generated.resources.symptom_blows_through_travel_description
import shimstackmultiplatform.composeapp.generated.resources.symptom_blows_through_travel_title
import shimstackmultiplatform.composeapp.generated.resources.symptom_brake_dive_description
import shimstackmultiplatform.composeapp.generated.resources.symptom_brake_dive_title
import shimstackmultiplatform.composeapp.generated.resources.symptom_frequent_bottom_out_description
import shimstackmultiplatform.composeapp.generated.resources.symptom_frequent_bottom_out_title
import shimstackmultiplatform.composeapp.generated.resources.symptom_front_flip_on_jumps_description
import shimstackmultiplatform.composeapp.generated.resources.symptom_front_flip_on_jumps_title
import shimstackmultiplatform.composeapp.generated.resources.symptom_harsh_description
import shimstackmultiplatform.composeapp.generated.resources.symptom_harsh_title
import shimstackmultiplatform.composeapp.generated.resources.symptom_mush_description
import shimstackmultiplatform.composeapp.generated.resources.symptom_mush_title
import shimstackmultiplatform.composeapp.generated.resources.symptom_oversteer_description
import shimstackmultiplatform.composeapp.generated.resources.symptom_oversteer_title
import shimstackmultiplatform.composeapp.generated.resources.symptom_packing_down_description
import shimstackmultiplatform.composeapp.generated.resources.symptom_packing_down_title
import shimstackmultiplatform.composeapp.generated.resources.symptom_rare_full_travel_description
import shimstackmultiplatform.composeapp.generated.resources.symptom_rare_full_travel_title
import shimstackmultiplatform.composeapp.generated.resources.symptom_sluggish_handling_description
import shimstackmultiplatform.composeapp.generated.resources.symptom_sluggish_handling_title
import shimstackmultiplatform.composeapp.generated.resources.symptom_steep_dive_description
import shimstackmultiplatform.composeapp.generated.resources.symptom_steep_dive_title
import shimstackmultiplatform.composeapp.generated.resources.symptom_too_much_comp_description
import shimstackmultiplatform.composeapp.generated.resources.symptom_too_much_comp_title
import shimstackmultiplatform.composeapp.generated.resources.symptom_understeer_description
import shimstackmultiplatform.composeapp.generated.resources.symptom_understeer_title
import shimstackmultiplatform.composeapp.generated.resources.symptom_wallowy_description
import shimstackmultiplatform.composeapp.generated.resources.symptom_wallowy_title

@Single
class SetupSymptomViewMapper {
    fun map(setupSymptom: SetupSymptom): SetupSymptomView {
        val (name, description) =
            when (setupSymptom) {
                SetupSymptom.UNDERSTEER -> Pair(Res.string.symptom_understeer_title, Res.string.symptom_understeer_description)
                SetupSymptom.OVERSTEER -> Pair(Res.string.symptom_oversteer_title, Res.string.symptom_oversteer_description)
                SetupSymptom.MUSH -> Pair(Res.string.symptom_mush_title, Res.string.symptom_mush_description)
                SetupSymptom.HARSH_OVER_SMALL_BUMPS -> Pair(Res.string.symptom_harsh_title, Res.string.symptom_harsh_description)
                SetupSymptom.BRAKE_DIVE -> Pair(Res.string.symptom_brake_dive_title, Res.string.symptom_brake_dive_description)
                SetupSymptom.STEEP_DIVE -> Pair(Res.string.symptom_steep_dive_title, Res.string.symptom_steep_dive_description)
                SetupSymptom.FREQUENT_BOTTOM_OUT ->
                    Pair(
                        Res.string.symptom_frequent_bottom_out_title,
                        Res.string.symptom_frequent_bottom_out_description
                    )

                SetupSymptom.RARE_FULL_TRAVEL ->
                    Pair(
                        Res.string.symptom_rare_full_travel_title,
                        Res.string.symptom_rare_full_travel_description
                    )

                SetupSymptom.FRONT_FLIP_ON_TAKE_OFF ->
                    Pair(
                        Res.string.symptom_front_flip_on_jumps_title,
                        Res.string.symptom_front_flip_on_jumps_description
                    )

                SetupSymptom.BIKE_PACKING_DOWN ->
                    Pair(
                        Res.string.symptom_packing_down_title,
                        Res.string.symptom_packing_down_description
                    )

                SetupSymptom.BIKE_BLOWS_THROUGH_TRAVEL ->
                    Pair(
                        Res.string.symptom_blows_through_travel_title,
                        Res.string.symptom_blows_through_travel_description
                    )

                SetupSymptom.SLUGGISH_HANDLING ->
                    Pair(
                        Res.string.symptom_sluggish_handling_title,
                        Res.string.symptom_sluggish_handling_description
                    )

                SetupSymptom.WALLLOWY -> Pair(Res.string.symptom_wallowy_title, Res.string.symptom_wallowy_description)
                SetupSymptom.BIKE_UNCOMFORTABLE ->
                    Pair(
                        Res.string.symptom_too_much_comp_title,
                        Res.string.symptom_too_much_comp_description
                    )
            }

        return SetupSymptomView(name, description, setupSymptom)
    }
}

data class SetupSymptomView(
    val name: StringResource,
    val description: StringResource,
    val setupSymptom: SetupSymptom,
    val selected: Boolean = false
)