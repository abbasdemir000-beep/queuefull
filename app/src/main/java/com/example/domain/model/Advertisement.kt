package com.example.domain.model

data class Advertisement(
    val id: Int = 0,
    val title: String,
    val description: String,
    val imageUrl: String? = null,
    val businessName: String,
    val category: AdCategory,
    val targetCity: String,
    val isActive: Boolean = true,
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long,
    val clickCount: Int = 0,
    val viewCount: Int = 0
)
