package com.example.domain.usecase

import com.example.domain.model.AppUser

/**
 * Pure auth/role decision logic for QueueFuel.
 *
 * Framework-agnostic (no Android/Compose). Extracted from the ViewModel so
 * it can be unit-tested and later moved server-side when real OTP is implemented.
 *
 * The "1234" backdoor OTP is intentionally preserved for MVP/testing.
 */
object AuthPolicy {

    /** The hardcoded admin phone number for the MVP. */
    const val ADMIN_PHONE: String = "07774564334"

    /** Minimum phone number length required for sending OTP. */
    const val MIN_PHONE_LENGTH: Int = 10

    /** OTP backdoor code accepted during MVP/testing regardless of the real OTP. */
    const val BACKDOOR_OTP: String = "1234"

    /**
     * True if the phone number meets the minimum length requirement.
     * Mirrors the original `authPhoneInput.length < 10` check (inverted).
     */
    fun isValidPhone(phone: String): Boolean = phone.length >= MIN_PHONE_LENGTH

    /**
     * True if the user name is non-blank (required before sending OTP).
     */
    fun isValidName(name: String): Boolean = name.trim().isNotBlank()

    /**
     * Generates a 4-digit OTP string (1000..9999).
     * Identical to the original inline generation.
     */
    fun generateOtp(): String = (1000..9999).random().toString()

    /**
     * True if the entered OTP matches either the expected OTP or the backdoor.
     * Mirrors: `authOtpInput != simulatedOtp && authOtpInput != "1234"` (inverted).
     */
    fun isOtpValid(input: String, expected: String): Boolean =
        input == expected || input == BACKDOOR_OTP

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
