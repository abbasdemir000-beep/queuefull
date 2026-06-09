package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "advertisements")
data class AdvertisementEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val imageUrl: String? = null,
    val businessName: String,
    val category: String,
    val targetCity: String,
    val isActive: Boolean = true,
    val startDate: Long,
    val endDate: Long,
    val clickCount: Int = 0,
    val viewCount: Int = 0
)
