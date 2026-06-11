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
     * True if the phone number (ignoring surrounding whitespace) consists of
     * digits only and meets the minimum length requirement.
     */
    fun isValidPhone(phone: String): Boolean {
        val trimmed = phone.trim()
        return trimmed.length >= MIN_PHONE_LENGTH && trimmed.all { it.isDigit() }
    }

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
     * Resolves the effective role once a backend exists: a server-assigned
     * role (`users/{uid}.role` in Firestore, written only by Cloud Functions /
     * the console) always wins; the hardcoded admin phone remains only as the
     * offline-demo fallback when no backend is configured.
     */
    fun effectiveRole(serverRole: String?, phone: String): String =
        serverRole?.takeIf { it.isNotBlank() } ?: resolveRole(phone)

    /**
     * True if the user account is banned and should be rejected at login.
     */
    fun isUserBanned(user: AppUser): Boolean = user.banned
}
