package com.example.ridertipstracker.data.local.db

import androidx.room.TypeConverter
import com.example.ridertipstracker.data.model.Platform

/**
 * Type converter for Platform enum.
 */
class PlatformTypeConverter {
    @TypeConverter
    fun fromPlatform(platform: Platform): String {
        return when (platform) {
            Platform.UBER_EATS -> "Uber Eats"
            Platform.LIEFERANDO -> "Lieferando"
            Platform.FLINK -> "Flink"
        }
    }

    @TypeConverter
    fun toPlatform(platformName: String): Platform {
        return when (platformName) {
            "Uber Eats" -> Platform.UBER_EATS
            "Lieferando" -> Platform.LIEFERANDO
            "Flink" -> Platform.FLINK
            else -> Platform.UBER_EATS // Default fallback
        }
    }
}
