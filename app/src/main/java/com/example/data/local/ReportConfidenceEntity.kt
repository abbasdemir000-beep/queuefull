package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "report_confidence")
data class ReportConfidenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val stationId: Int,
    val originalReporterId: String,
    val confirmationCount: Int = 0,
    val confidenceScore: Float = 1.0f,
    val timestamp: Long = System.currentTimeMillis(),
    val expiresAt: Long,
    val isExpired: Boolean = false
)
