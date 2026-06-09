package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_badges")
data class UserBadgeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userPhone: String,
    val badge: String,
    val awardedAt: Long = System.currentTimeMillis()
)
