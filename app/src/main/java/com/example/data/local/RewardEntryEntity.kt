package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reward_entries")
data class RewardEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userPhone: String,
    val stationId: Int,
    val reportId: Int,
    val monthYear: String,
    val isVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
