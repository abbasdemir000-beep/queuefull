package com.example.domain.model

data class ReportConfidence(
    val id: Int = 0,
    val stationId: Int,
    val originalReporterId: String,
    val confirmationCount: Int = 0,
    val confidenceScore: Float = 1.0f,
    val timestamp: Long = System.currentTimeMillis(),
    val expiresAt: Long,
    val isExpired: Boolean = false
)
