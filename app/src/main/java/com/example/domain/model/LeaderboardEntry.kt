package com.example.domain.model

data class LeaderboardEntry(
    val rank: Int,
    val userPhone: String,
    val userName: String,
    val monthlyPoints: Int,
    val badge: Badge?
)
