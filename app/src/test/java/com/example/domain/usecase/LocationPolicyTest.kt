package com.example.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationPolicyTest {

    private val fix = LocationPolicy.RealFix(latitude = 35.5, longitude = 44.4, timestampMs = 1_000_000L)

    // ---- Freshness ----

    @Test
    fun `fix is fresh up to and including the max age boundary`() {
        assertTrue(LocationPolicy.isFresh(fix, fix.timestampMs))
        assertTrue(LocationPolicy.isFresh(fix, fix.timestampMs + LocationPolicy.MAX_FIX_AGE_MS))
    }

    @Test
    fun `fix is stale past the max age boundary`() {
        assertFalse(LocationPolicy.isFresh(fix, fix.timestampMs + LocationPolicy.MAX_FIX_AGE_MS + 1))
    }

    // ---- Effective coordinates ----

    @Test
    fun `fresh real fix wins over simulation`() {
        val coords = LocationPolicy.effectiveCoordinates(fix, 1.0, 2.0, fix.timestampMs + 1)
        assertEquals(35.5, coords.latitude, 0.0)
        assertEquals(44.4, coords.longitude, 0.0)
        assertTrue(coords.isReal)
    }

    @Test
    fun `missing fix falls back to simulated coordinates`() {
        val coords = LocationPolicy.effectiveCoordinates(null, 1.0, 2.0, 0L)
        assertEquals(1.0, coords.latitude, 0.0)
        assertEquals(2.0, coords.longitude, 0.0)
        assertFalse(coords.isReal)
    }

    @Test
    fun `stale fix falls back to simulated coordinates`() {
        val now = fix.timestampMs + LocationPolicy.MAX_FIX_AGE_MS + 1
        val coords = LocationPolicy.effectiveCoordinates(fix, 1.0, 2.0, now)
        assertFalse(coords.isReal)
        assertEquals(1.0, coords.latitude, 0.0)
    }

    // ---- Geofence bypass rules ----

    @Test
    fun `simulated position with simulation disabled may bypass the geofence`() {
        val simulated = LocationPolicy.Coordinates(1.0, 2.0, isReal = false)
        assertTrue(LocationPolicy.mayBypassGeofence(simulated, simulationEnabled = false))
    }

    @Test
    fun `simulated position with simulation enabled enforces the geofence`() {
        val simulated = LocationPolicy.Coordinates(1.0, 2.0, isReal = false)
        assertFalse(LocationPolicy.mayBypassGeofence(simulated, simulationEnabled = true))
    }

    @Test
    fun `real position never bypasses the geofence`() {
        val real = LocationPolicy.Coordinates(1.0, 2.0, isReal = true)
        assertFalse(LocationPolicy.mayBypassGeofence(real, simulationEnabled = false))
        assertFalse(LocationPolicy.mayBypassGeofence(real, simulationEnabled = true))
    }
}
