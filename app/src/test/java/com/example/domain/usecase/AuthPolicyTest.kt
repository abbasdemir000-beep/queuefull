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

    // ---- OTP generation ----

    @Test
    fun `generated OTP is 4 digits`() {
        repeat(50) {
            val otp = AuthPolicy.generateOtp()
            assertEquals(4, otp.length)
            assertTrue(otp.toInt() in 1000..9999)
        }
    }

    // ---- OTP verification ----

    @Test
    fun `correct OTP is accepted`() {
        assertTrue(AuthPolicy.isOtpValid("5678", "5678"))
    }

    @Test
    fun `backdoor 1234 is always accepted`() {
        assertTrue(AuthPolicy.isOtpValid("1234", "9999"))
        assertTrue(AuthPolicy.isOtpValid("1234", ""))
    }

    @Test
    fun `wrong OTP is rejected`() {
        assertFalse(AuthPolicy.isOtpValid("0000", "5678"))
        assertFalse(AuthPolicy.isOtpValid("", "5678"))
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
