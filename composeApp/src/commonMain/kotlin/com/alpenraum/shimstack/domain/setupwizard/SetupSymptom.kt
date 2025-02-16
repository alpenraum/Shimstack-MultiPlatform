package com.alpenraum.shimstack.domain.setupwizard

enum class SetupSymptom(
    val requiresLocation: Boolean = false,
    val requiresSpeed: Boolean = false
) {
    UNDERSTEER, // front rear tire balance - reduce front tire pressure IF NOT TOO LOW
    // - if not better through pressure then bring more weight to front by lowering handlebar

    OVERSTEER, // front rear tire balance - reduce rear tire pressure IF NOT TOO LOW

    SLUGGISH_HANDLING, // front Tire pressure too low

    WALLLOWY, // both Tire pressure too low

    MUSH(requiresLocation = true), // not enough pressure

    HARSH_OVER_SMALL_BUMPS(requiresLocation = true), // too much pressure

    BRAKE_DIVE, // not enough front pressure

    STEEP_DIVE, // not enough front pressure

    FREQUENT_BOTTOM_OUT(requiresSpeed = true, requiresLocation = true), // IF pressure is good then more tokens else more pressure

    RARE_FULL_TRAVEL(requiresSpeed = true, requiresLocation = true), // IF pressure is good then less tokens else less pressure

    FRONT_FLIP_ON_TAKE_OFF, // Imbalance between F and R rebound, slow down rear rebound or speed up front

    BIKE_PACKING_DOWN(requiresSpeed = true, requiresLocation = true), // lsr faster

    BIKE_BLOWS_THROUGH_TRAVEL(requiresSpeed = true, requiresLocation = true), // more compression

    BIKE_UNCOMFORTABLE(requiresSpeed = true, requiresLocation = true) // less compression
}

enum class SetupAnalysisResult {
    OK,
    TOO_LOW,
    TOO_HIGH
}