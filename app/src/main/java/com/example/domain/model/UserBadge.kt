package com.example.domain.model

data class UserBadge(
    val id: Int = 0,
    val userPhone: String,
    val badge: Badge,
    val awardedAt: Long = System.currentTimeMillis()
)
