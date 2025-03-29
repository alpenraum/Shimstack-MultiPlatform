package com.alpenraum.shimstack.data.userSettings

import com.alpenraum.shimstack.base.logger.ShimstackLogger
import com.alpenraum.shimstack.base.logger.WithLogger
import com.alpenraum.shimstack.data.datastore.ShimstackDatastore
import com.alpenraum.shimstack.domain.model.PreferredTheme
import com.alpenraum.shimstack.domain.model.measurementunit.MeasurementUnitType
import com.alpenraum.shimstack.domain.userSettings.UserSettings
import com.alpenraum.shimstack.domain.userSettings.UserSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.koin.core.annotation.Single

@Single
class UserSettingsDataStoreRepository(
    private val dataStore: ShimstackDatastore,
    logger: ShimstackLogger
) : WithLogger(logger),
    UserSettingsRepository {
    override fun getUserSettings(): Flow<UserSettings> =
        combine(
            dataStore.allowAnalytics,
            dataStore.preferredTheme,
            dataStore.measurementUnitType,
            dataStore.isOnboardingCompleted
        ) { x1, x2, x3, x4 ->
            val measurementUnitType =
                try {
                    MeasurementUnitType.valueOf(x3)
                } catch (e: IllegalArgumentException) {
                    MeasurementUnitType.METRIC
                }

            val preferredTheme =
                try {
                    PreferredTheme.valueOf(x2)
                } catch (e: IllegalArgumentException) {
                    PreferredTheme.default
                }
            UserSettings(
                preferredTheme = preferredTheme,
                isAnalyticsEnabled = x1,
                measurementUnitType = measurementUnitType,
                isOnboardingCompleted = x4
            )
        }

    override suspend fun updatePreferredThemeEnabled(type: PreferredTheme) {
        dataStore.setPreferredTheme(type.name)
    }

    override suspend fun updateMeasurementUnitType(type: MeasurementUnitType) {
        dataStore.setMeasurementUnit(type.name)
    }

    override suspend fun updateIsAnalyticsEnabled(enabled: Boolean) {
        dataStore.setAllowAnalytics(enabled)
    }

    override suspend fun updateIsOnboardingCompleted(enabled: Boolean) {
        dataStore.setIsOnboardingCompleted(enabled)
    }
}