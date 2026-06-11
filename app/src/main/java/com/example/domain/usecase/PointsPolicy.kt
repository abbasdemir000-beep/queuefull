package com.example.domain.usecase

/**
 * Single source of truth for the rewards/points economy.
 *
 * Pure and framework-agnostic. Both the ViewModel and the repository award
 * points exclusively through these constants.
 */
object PointsPolicy {

    /** Awarded for submitting a fresh queue status update. */
    const val REPORT_POINTS: Int = 15

    /** Awarded for confirming an existing queue status. */
    const val CONFIRMATION_POINTS: Int = 5

    /** Awarded to the suggester when they submit a new station. */
    const val STATION_SUGGESTION_POINTS: Int = 30

    /** Awarded to the suggester when an admin approves their station. */
    const val STATION_APPROVAL_POINTS: Int = 50

    /** Starting balance granted to a newly registered (non-admin) user. */
    const val WELCOME_POINTS: Int = 20

    /** Balance every non-admin user is reset to at the start of a new cycle. */
    const val CYCLE_RESET_POINTS: Int = 20

    /** Starting balance granted to the admin account. */
    const val ADMIN_INITIAL_POINTS: Int = 250

    /** Adds [amount] to a current balance. Pure helper for future reuse/testing. */
    fun award(currentPoints: Int, amount: Int): Int = currentPoints + amount
}
