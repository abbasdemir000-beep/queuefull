package com.example.domain.model

data class AppUser(
    val phoneNumber: String,
    val name: String,
    val role: String = "USER",
    val points: Int = 0,
    val banned: Boolean = false,
    val lifetimePoints: Int = 0,
    val monthlyPoints: Int = 0,
    val accountType: String = AccountType.FREE.name,
    val premiumUntil: Long? = null,
    val city: String = "",
    val phoneVerified: Boolean = false
)
