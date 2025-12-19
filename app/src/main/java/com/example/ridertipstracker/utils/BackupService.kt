package com.example.ridertipstracker.utils

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import com.example.ridertipstracker.data.local.entity.*
import com.example.ridertipstracker.repository.RiderShiftRepository
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: RiderShiftRepository
) {
    suspend fun createBackup(uri: Uri): Boolean {
        return try {
            val outputStream = context.contentResolver.openOutputStream(uri) ?: return false
            val writer = OutputStreamWriter(outputStream)
            
            val backupData = JSONObject().apply {
                put("version", 1)
                put("timestamp", System.currentTimeMillis())
                
                // Backup shifts
                val shifts = repository.getAllShifts().first()
                val shiftsArray = JSONArray()
                shifts.forEach { shift ->
                    shiftsArray.put(JSONObject().apply {
                        put("id", shift.id)
                        put("date", shift.date.toString())
                        put("platform", shift.platform)
                        put("shiftStartTime", shift.shiftStartTime.toString())
                        put("shiftEndTime", shift.shiftEndTime.toString())
                        put("totalHours", shift.totalHours)
                        put("onlineTips", shift.onlineTips)
                        put("cashTips", shift.cashTips)
                        put("totalTips", shift.totalTips)
                        put("orders", shift.orders)
                        put("shiftType", shift.shiftType)
                        put("notes", shift.notes ?: "")
                    })
                }
                put("shifts", shiftsArray)
            }
            
            writer.write(backupData.toString())
            writer.close()
            outputStream.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun restoreBackup(uri: Uri): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return false
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val backupData = JSONObject(jsonString)
            
            val shiftsArray = backupData.getJSONArray("shifts")
            val shifts = mutableListOf<RiderShift>()
            
            for (i in 0 until shiftsArray.length()) {
                val shiftObj = shiftsArray.getJSONObject(i)
                shifts.add(
                    RiderShift(
                        id = 0, // Reset ID for new import
                        date = java.time.LocalDate.parse(shiftObj.getString("date")),
                        platform = shiftObj.getString("platform"),
                        shiftStartTime = java.time.LocalTime.parse(shiftObj.getString("shiftStartTime")),
                        shiftEndTime = java.time.LocalTime.parse(shiftObj.getString("shiftEndTime")),
                        totalHours = shiftObj.getDouble("totalHours"),
                        onlineTips = shiftObj.getDouble("onlineTips"),
                        cashTips = shiftObj.getDouble("cashTips"),
                        totalTips = shiftObj.getDouble("totalTips"),
                        orders = shiftObj.getInt("orders"),
                        shiftType = shiftObj.getString("shiftType"),
                        notes = if (shiftObj.has("notes")) shiftObj.getString("notes") else null
                    )
                )
            }
            
            repository.insertShifts(shifts)
            inputStream.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

