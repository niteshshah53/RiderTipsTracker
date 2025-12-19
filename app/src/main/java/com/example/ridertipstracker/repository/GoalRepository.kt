package com.example.ridertipstracker.repository

import com.example.ridertipstracker.data.local.dao.GoalDao
import com.example.ridertipstracker.data.local.entity.Goal
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepository @Inject constructor(
    private val goalDao: GoalDao
) {
    fun getAllActiveGoals(): Flow<List<Goal>> = goalDao.getAllActiveGoals()
    fun getAllGoals(): Flow<List<Goal>> = goalDao.getAllGoals()
    suspend fun insertGoal(goal: Goal) = goalDao.insertGoal(goal)
    suspend fun updateGoal(goal: Goal) = goalDao.updateGoal(goal)
    suspend fun deleteGoal(goal: Goal) = goalDao.deleteGoal(goal)
    fun getActiveGoalByType(type: String, date: LocalDate = LocalDate.now()): Flow<Goal?> = 
        goalDao.getActiveGoalByType(type, date)
}

