package com.example.ridertipstracker.data.local.dao

import androidx.room.*
import com.example.ridertipstracker.data.local.entity.NotificationPreference
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationPreferenceDao {
    @Query("SELECT * FROM notification_preferences")
    fun getAllPreferences(): Flow<List<NotificationPreference>>

    @Query("SELECT * FROM notification_preferences WHERE id = :id")
    suspend fun getPreference(id: String): NotificationPreference?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreference(preference: NotificationPreference)

    @Update
    suspend fun updatePreference(preference: NotificationPreference)

    @Query("UPDATE notification_preferences SET isEnabled = :enabled WHERE id = :id")
    suspend fun setPreferenceEnabled(id: String, enabled: Boolean)
}

