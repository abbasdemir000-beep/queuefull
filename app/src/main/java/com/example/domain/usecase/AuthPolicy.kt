package com.example.domain.usecase

import com.example.domain.model.AppUser

/**
 * Pure auth/role decision logic for QueueFuel.
 *
 * Framework-agnostic (no Android/Compose), unit-tested.
 *
 * MVP login is a simple profile registration: name + phone + city, saved
 * directly with `phoneVerified = false`. There is no OTP, no SMS, and no
 * Firebase Phone Auth. Real phone verification can be added server-side later
 * by flipping `phoneVerified` on the user.
 */
object AuthPolicy {

    /** The hardcoded admin phone number for the MVP. */
    const val ADMIN_PHONE: String = "07774564334"

    /** Minimum phone number length required to register. */
    const val MIN_PHONE_LENGTH: Int = 10

    /**
     * True if the phone number meets the minimum length requirement.
     */
    fun isValidPhone(phone: String): Boolean = phone.length >= MIN_PHONE_LENGTH

    /**
     * True if the user name is non-blank (required for registration).
     */
    fun isValidName(name: String): Boolean = name.trim().isNotBlank()

    /**
     * True when a registration form (name + phone + selected city) is complete
     * and valid enough to save the user directly.
     */
    fun canRegister(name: String, phone: String, cityId: Int?): Boolean =
        isValidName(name) && isValidPhone(phone) && cityId != null

    /**
     * Resolves the role for a phone number.
     * The admin phone always gets "ADMIN"; everyone else gets "USER".
     */
    fun resolveRole(phone: String): String =
        if (phone == ADMIN_PHONE) "ADMIN" else "USER"

    /**
     * True if the phone belongs to the designated admin.
     */
    fun isAdminPhone(phone: String): Boolean = phone == ADMIN_PHONE

    /**
     * True if the user account is banned and should be rejected at login.
     */
    fun isUserBanned(user: AppUser): Boolean = user.banned
}
