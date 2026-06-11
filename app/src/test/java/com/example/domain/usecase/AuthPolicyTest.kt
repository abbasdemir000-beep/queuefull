package com.example.domain.usecase

import com.example.domain.model.AppUser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthPolicyTest {

    // ---- Phone validation ----

    @Test
    fun `valid phone has at least 10 characters`() {
        assertTrue(AuthPolicy.isValidPhone("07774564334")) // 11 chars
        assertTrue(AuthPolicy.isValidPhone("0777456433"))  // exactly 10
    }

    @Test
    fun `phone shorter than 10 characters is invalid`() {
        assertFalse(AuthPolicy.isValidPhone("077745643")) // 9 chars
        assertFalse(AuthPolicy.isValidPhone(""))
    }

    @Test
    fun `phone containing non-digits is invalid`() {
        assertFalse(AuthPolicy.isValidPhone("abcdefghij"))
        assertFalse(AuthPolicy.isValidPhone("0777-456-433"))
        assertFalse(AuthPolicy.isValidPhone("0777 456 433"))
    }

    @Test
    fun `surrounding whitespace is ignored`() {
        assertTrue(AuthPolicy.isValidPhone(" 07774564334 "))
    }

    // ---- Name validation ----

    @Test
    fun `non-blank name is valid`() {
        assertTrue(AuthPolicy.isValidName("أحمد"))
        assertTrue(AuthPolicy.isValidName(" Abbas ")) // leading/trailing spaces trimmed
    }

    @Test
    fun `blank or whitespace-only name is invalid`() {
        assertFalse(AuthPolicy.isValidName(""))
        assertFalse(AuthPolicy.isValidName("   "))
        assertFalse(AuthPolicy.isValidName("\t\n"))
    }

    // ---- Registration form validation (no OTP in MVP) ----

    @Test
    fun `complete registration form is accepted`() {
        assertTrue(AuthPolicy.canRegister("أحمد", "07712345678", 1))
    }

    @Test
    fun `registration without a city is rejected`() {
        assertFalse(AuthPolicy.canRegister("أحمد", "07712345678", null))
    }

    @Test
    fun `registration with blank name or short phone is rejected`() {
        assertFalse(AuthPolicy.canRegister("  ", "07712345678", 1))
        assertFalse(AuthPolicy.canRegister("أحمد", "0771", 1))
    }

    // ---- Role resolution ----

    @Test
    fun `admin phone resolves to ADMIN role`() {
        assertEquals("ADMIN", AuthPolicy.resolveRole("07774564334"))
    }

    @Test
    fun `any other phone resolves to USER role`() {
        assertEquals("USER", AuthPolicy.resolveRole("07712345678"))
        assertEquals("USER", AuthPolicy.resolveRole("00000000000"))
    }

    @Test
    fun `isAdminPhone matches only the admin number`() {
        assertTrue(AuthPolicy.isAdminPhone("07774564334"))
        assertFalse(AuthPolicy.isAdminPhone("07712345678"))
    }

    // ---- Effective role (server role wins over the hardcoded phone) ----

    @Test
    fun `server-assigned role overrides the local fallback`() {
        assertEquals("ADMIN", AuthPolicy.effectiveRole("ADMIN", "07712345678"))
        assertEquals("USER", AuthPolicy.effectiveRole("USER", "07774564334"))
    }

    @Test
    fun `missing or blank server role falls back to the phone-based role`() {
        assertEquals("ADMIN", AuthPolicy.effectiveRole(null, "07774564334"))
        assertEquals("ADMIN", AuthPolicy.effectiveRole("", "07774564334"))
        assertEquals("USER", AuthPolicy.effectiveRole(null, "07712345678"))
    }

    // ---- Ban check ----

    @Test
    fun `banned user is detected`() {
        val banned = AppUser(phoneNumber = "077", name = "X", banned = true)
        assertTrue(AuthPolicy.isUserBanned(banned))
    }

    @Test
    fun `non-banned user passes`() {
        val active = AppUser(phoneNumber = "077", name = "X", banned = false)
        assertFalse(AuthPolicy.isUserBanned(active))
    }
}
