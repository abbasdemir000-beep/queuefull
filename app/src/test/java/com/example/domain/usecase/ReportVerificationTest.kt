package com.example.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportVerificationTest {

    private val verifier = StubReportVerifier()

    // Kirkuk seed station coordinates
    private val stationLat = 35.4682
    private val stationLng = 44.3921

    private fun request(
        userLat: Double = stationLat,
        userLng: Double = stationLng,
        status: String = "EMPTY",
        photoPath: String? = "/data/photos/report.jpg"
    ) = ReportVerificationRequest(
        stationLatitude = stationLat,
        stationLongitude = stationLng,
        userLatitude = userLat,
        userLongitude = userLng,
        claimedStatus = status,
        photoPath = photoPath
    )

    @Test
    fun `report with photo at the station is verified`() = runTest {
        val result = verifier.verify(request())
        assertTrue(result.isVerified)
        assertEquals(ReportVerification.VERIFIED, result.verdict)
    }

    @Test
    fun `report without a photo is rejected`() = runTest {
        assertFalse(verifier.verify(request(photoPath = null)).isVerified)
        assertFalse(verifier.verify(request(photoPath = "")).isVerified)
    }

    @Test
    fun `report far from the station is rejected`() = runTest {
        // ~1.1 km north of the station — well outside the 200 m geofence
        val result = verifier.verify(request(userLat = stationLat + 0.01))
        assertEquals(ReportVerification.REJECTED, result.verdict)
    }

    @Test
    fun `report just inside the geofence is verified`() = runTest {
        // ~110 m north of the station
        val result = verifier.verify(request(userLat = stationLat + 0.001))
        assertTrue(result.isVerified)
    }

    @Test
    fun `all three congestion levels are reportable`() = runTest {
        for (status in listOf("EMPTY", "MODERATE", "LONG")) {
            assertTrue(verifier.verify(request(status = status)).isVerified)
        }
    }

    @Test
    fun `unknown congestion status is rejected`() = runTest {
        assertEquals(
            ReportVerification.REJECTED,
            verifier.verify(request(status = "WHATEVER")).verdict
        )
    }

    @Test
    fun `rejection always carries a reason`() = runTest {
        val result = verifier.verify(request(photoPath = null))
        assertTrue(result.reason.isNotBlank())
    }
}
