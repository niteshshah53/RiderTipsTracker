package com.example.ridertipstracker.data.local.dao

import androidx.room.*
import com.example.ridertipstracker.data.local.entity.Goal
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getAllActiveGoals(): Flow<List<Goal>>

    @Query("SELECT * FROM goals ORDER BY createdAt DESC")
    fun getAllGoals(): Flow<List<Goal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal)

    @Update
    suspend fun updateGoal(goal: Goal)

    @Delete
    suspend fun deleteGoal(goal: Goal)

    @Query("SELECT * FROM goals WHERE type = :type AND isActive = 1 AND startDate <= :date AND (endDate IS NULL OR endDate >= :date)")
    fun getActiveGoalByType(type: String, date: LocalDate): Flow<Goal?>
}

