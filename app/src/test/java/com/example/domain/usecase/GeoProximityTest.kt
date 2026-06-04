package com.example.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeoProximityTest {

    @Test
    fun `distance between identical coordinates is zero`() {
        val d = GeoProximity.distanceMeters(35.4682, 44.3921, 35.4682, 44.3921)
        assertEquals(0.0, d, 0.0001)
    }

    @Test
    fun `one degree of longitude at the equator is about 111 km`() {
        val d = GeoProximity.distanceMeters(0.0, 0.0, 0.0, 1.0)
        // ~111.195 km; generous tolerance to stay robust against float details.
        assertEquals(111_195.0, d, 100.0)
    }

    @Test
    fun `distance is symmetric`() {
        val ab = GeoProximity.distanceMeters(36.1915, 44.0094, 35.5601, 45.4208)
        val ba = GeoProximity.distanceMeters(35.5601, 45.4208, 36.1915, 44.0094)
        assertEquals(ab, ba, 0.0001)
    }

    @Test
    fun `default reporting radius is 200 meters`() {
        assertEquals(200.0, GeoProximity.DEFAULT_RADIUS_METERS, 0.0)
    }

    @Test
    fun `point roughly 111 meters away is within the default radius`() {
        // 0.001 deg of latitude ~= 111 m.
        assertTrue(GeoProximity.isWithinRadius(35.4682, 44.3921, 35.4692, 44.3921))
    }

    @Test
    fun `point roughly 222 meters away is outside the default radius`() {
        // 0.002 deg of latitude ~= 222 m.
        assertFalse(GeoProximity.isWithinRadius(35.4682, 44.3921, 35.4702, 44.3921))
    }

    @Test
    fun `radius boundary is inclusive`() {
        val a = 35.4682
        val b = 44.3921
        val c = 35.4692
        val d = 44.3921
        val exact = GeoProximity.distanceMeters(a, b, c, d)
        // Exactly equal to the distance -> inside (uses <=).
        assertTrue(GeoProximity.isWithinRadius(a, b, c, d, exact))
        // Just under the distance -> outside.
        assertFalse(GeoProximity.isWithinRadius(a, b, c, d, exact - 0.0001))
    }
}
