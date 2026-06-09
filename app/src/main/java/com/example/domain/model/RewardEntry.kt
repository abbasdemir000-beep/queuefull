package com.example.domain.model

data class RewardEntry(
    val id: Int = 0,
    val userPhone: String,
    val stationId: Int,
    val reportId: Int,
    val monthYear: String,
    val isVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
