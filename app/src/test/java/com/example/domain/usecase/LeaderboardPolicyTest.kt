package com.example.domain.usecase

import com.example.domain.model.AppUser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LeaderboardPolicyTest {

    private val users = listOf(
        AppUser("A", "Ali", monthlyPoints = 30, lifetimePoints = 120),
        AppUser("B", "Bakr", monthlyPoints = 60, lifetimePoints = 10),
        AppUser("C", "Carla", monthlyPoints = 15, lifetimePoints = 500)
    )

    @Test
    fun `users ranked by monthly points descending`() {
        val ranked = LeaderboardPolicy.rank(users)
        assertEquals(1, ranked[0].rank)
        assertEquals("B", ranked[0].userPhone)
        assertEquals(2, ranked[1].rank)
        assertEquals("A", ranked[1].userPhone)
        assertEquals(3, ranked[2].rank)
    }

    @Test
    fun `current user entry found by phone`() {
        val entry = LeaderboardPolicy.currentUserEntry(users, "C")
        assertEquals(3, entry?.rank)
        assertEquals(15, entry?.monthlyPoints)
    }

    @Test
    fun `unknown phone returns null`() {
        assertNull(LeaderboardPolicy.currentUserEntry(users, "Z"))
    }

    @Test
    fun `badge assigned based on lifetime points`() {
        val ranked = LeaderboardPolicy.rank(users)
        val legendEntry = ranked.first { it.userPhone == "C" }
        assertEquals(com.example.domain.model.Badge.LEGEND, legendEntry.badge)
    }
}
