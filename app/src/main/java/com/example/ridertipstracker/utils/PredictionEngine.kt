package com.example.ridertipstracker.utils

import com.example.ridertipstracker.data.local.entity.RiderShift
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.sqrt

data class PredictionResult(
    val predictedTotalTips: Double,
    val minRange: Double,
    val maxRange: Double,
    val confidence: ConfidenceLevel,
    val explanation: String
)

enum class ConfidenceLevel { LOW, MEDIUM, HIGH }

class PredictionEngine {

    fun predictNextWeek(allShifts: List<RiderShift>): PredictionResult {
        if (allShifts.isEmpty()) {
            return PredictionResult(0.0, 0.0, 0.0, ConfidenceLevel.LOW, "Not enough data to make a prediction.")
        }

        val today = LocalDate.now()
        val eightWeeksAgo = today.minusWeeks(8)
        
        // Filter shifts from last 8 weeks
        val recentShifts = allShifts.filter { it.date.isAfter(eightWeeksAgo) }
        
        if (recentShifts.isEmpty()) {
            return PredictionResult(0.0, 0.0, 0.0, ConfidenceLevel.LOW, "No recent data found in the last 8 weeks.")
        }

        // Group shifts by week to calculate weekly totals
        val weeklyTotals = recentShifts.groupBy { 
            ChronoUnit.WEEKS.between(it.date, today) 
        }.mapValues { entry -> 
            entry.value.sumOf { it.totalTips }
        }

        // Weighted Moving Average calculation
        // Recent weeks get higher weights: Week 0 (this week so far) = 1.0, Week 1 = 0.8, Week 2 = 0.6, etc.
        var weightedSum = 0.0
        var totalWeight = 0.0
        
        for (i in 0..7) {
            val total = weeklyTotals[i.toLong()] ?: continue
            val weight = 1.0 - (i * 0.1)
            if (weight <= 0) break
            
            weightedSum += total * weight
            totalWeight += weight
        }

        val predictedTips = if (totalWeight > 0) weightedSum / totalWeight else 0.0
        
        // Calculate standard deviation for range and confidence
        val tipValues = weeklyTotals.values
        val mean = if (tipValues.isNotEmpty()) tipValues.average() else 0.0
        val variance = if (tipValues.size > 1) {
            tipValues.sumOf { Math.pow(it - mean, 2.0) } / (tipValues.size - 1)
        } else 0.0
        val stdDev = sqrt(variance)

        // Determine confidence and range
        val confidence = when {
            tipValues.size >= 6 && stdDev < mean * 0.2 -> ConfidenceLevel.HIGH
            tipValues.size >= 4 -> ConfidenceLevel.MEDIUM
            else -> ConfidenceLevel.LOW
        }

        val rangeMultiplier = if (confidence == ConfidenceLevel.HIGH) 1.0 else 1.5
        val minRange = maxOf(0.0, predictedTips - (stdDev * rangeMultiplier))
        val maxRange = predictedTips + (stdDev * rangeMultiplier)

        val explanation = buildExplanation(confidence, tipValues.size, predictedTips)

        return PredictionResult(
            predictedTotalTips = predictedTips,
            minRange = minRange,
            maxRange = maxRange,
            confidence = confidence,
            explanation = explanation
        )
    }

    private fun buildExplanation(confidence: ConfidenceLevel, dataPoints: Int, predicted: Double): String {
        return when (confidence) {
            ConfidenceLevel.HIGH -> "Based on your consistent earnings over $dataPoints weeks, we're very confident you'll earn around €${String.format("%.2f", predicted)} next week."
            ConfidenceLevel.MEDIUM -> "Your earnings fluctuate slightly. You're likely to see about €${String.format("%.2f", predicted)}, assuming a similar schedule."
            ConfidenceLevel.LOW -> "Data is limited. This is a rough estimate based on what we've tracked so far."
        }
    }
}
