package com.alpenraum.shimstack.data.model.setupRecommendation

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.alpenraum.shimstack.data.db.AppDatabase
import com.alpenraum.shimstack.data.model.bike.BikeDTO
import com.alpenraum.shimstack.domain.setupwizard.SetupRecommendation
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = AppDatabase.TABLE_SETUP_RECOMMENDATION,
    foreignKeys = [
        ForeignKey(
            entity = BikeDTO::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("bikeId"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ]
)
class SetupRecommendationDTO(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    @ColumnInfo(index = true) val bikeId: Int,
    val wizardSession: String,
    val creation_epoch_seconds: Long = Clock.System.now().epochSeconds,
    val frontTirePressureDelta: Double? = null,
    val rearTirePressureDelta: Double? = null,
    val frontSagDelta: Double? = null,
    val rearSagDelta: Double? = null,
    val frontHSCDelta: Int? = null,
    val rearHSCDelta: Int? = null,
    val frontLSCDelta: Int? = null,
    val rearLSCDelta: Int? = null,
    val frontHSRDelta: Int? = null,
    val rearHSRDelta: Int? = null,
    val frontLSRDelta: Int? = null,
    val rearLSRDelta: Int? = null,
    val frontTokenDelta: Int? = null,
    val rearTokenDelta: Int? = null,
    val isAccepted: Boolean? = null
) {
    fun toDomain() =
        SetupRecommendation(
            id,
            bikeId,
            wizardSession,
            frontTirePressureDelta,
            rearTirePressureDelta,
            frontSagDelta,
            rearSagDelta,
            frontHSCDelta,
            rearHSCDelta,
            frontLSCDelta,
            rearLSCDelta,
            frontHSRDelta,
            rearHSRDelta,
            frontLSRDelta,
            rearLSRDelta,
            frontTokenDelta,
            rearTokenDelta,
            isAccepted
        )
}