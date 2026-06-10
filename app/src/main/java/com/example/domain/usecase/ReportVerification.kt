package com.example.domain.usecase

/**
 * Verification verdicts for user-submitted queue reports.
 *
 * Every report must pass verification (photo + location + claimed congestion)
 * before it affects the station status, awards points, or enters the raffle.
 */
object ReportVerification {
    const val PENDING = "PENDING"
    const val VERIFIED = "VERIFIED"
    const val REJECTED = "REJECTED"

    /** Congestion levels a user may claim in a report (Empty / Medium / Crowded). */
    val REPORTABLE_STATUSES: Set<String> = setOf("EMPTY", "MODERATE", "LONG")
}

/** Everything a verifier needs to judge a single report. */
data class ReportVerificationRequest(
    val stationLatitude: Double,
    val stationLongitude: Double,
    val userLatitude: Double,
    val userLongitude: Double,
    val claimedStatus: String,
    val photoPath: String?
)

data class ReportVerificationResult(
    val verdict: String,
    val reason: String,
    val confidence: Float
) {
    val isVerified: Boolean get() = verdict == ReportVerification.VERIFIED
}

/**
 * Pluggable report verifier.
 *
 * MVP ships with [StubReportVerifier] (deterministic heuristics). A real AI
 * implementation (e.g. a vision model that checks the photo against the
 * station location and the claimed congestion level) can replace it later by
 * implementing this interface — no caller changes needed.
 */
interface ReportVerifier {
    suspend fun verify(request: ReportVerificationRequest): ReportVerificationResult
}

/**
 * Deterministic MVP verifier. Approves a report only when:
 *  1. a photo is attached,
 *  2. the reporter's GPS position is inside the station geofence (~200 m),
 *  3. the claimed congestion level is one of the reportable statuses.
 *
 * It cannot inspect photo content — that is the job of the future AI verifier.
 */
class StubReportVerifier : ReportVerifier {

    override suspend fun verify(request: ReportVerificationRequest): ReportVerificationResult {
        if (request.photoPath.isNullOrBlank()) {
            return ReportVerificationResult(
                verdict = ReportVerification.REJECTED,
                reason = "لا توجد صورة مرفقة بالتقرير",
                confidence = 1.0f
            )
        }

        if (request.claimedStatus !in ReportVerification.REPORTABLE_STATUSES) {
            return ReportVerificationResult(
                verdict = ReportVerification.REJECTED,
                reason = "حالة الازدحام المحددة غير صالحة",
                confidence = 1.0f
            )
        }

        val distance = GeoProximity.distanceMeters(
            request.userLatitude, request.userLongitude,
            request.stationLatitude, request.stationLongitude
        )
        if (distance > GeoProximity.DEFAULT_RADIUS_METERS) {
            return ReportVerificationResult(
                verdict = ReportVerification.REJECTED,
                reason = "موقعك (${distance.toInt()}م) خارج نطاق المحطة، لا يمكن مطابقة الصورة مع الموقع",
                confidence = 1.0f
            )
        }

        return ReportVerificationResult(
            verdict = ReportVerification.VERIFIED,
            reason = "صورة مرفقة وموقع مطابق للمحطة",
            confidence = 0.8f
        )
    }
}
