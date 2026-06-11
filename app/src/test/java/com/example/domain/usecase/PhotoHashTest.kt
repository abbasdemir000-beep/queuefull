package com.example.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class PhotoHashTest {

    @Test
    fun `empty input matches the well-known SHA-256 vector`() {
        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            PhotoHash.sha256Hex(ByteArray(0))
        )
    }

    @Test
    fun `abc matches the well-known SHA-256 vector`() {
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            PhotoHash.sha256Hex("abc".toByteArray(Charsets.UTF_8))
        )
    }

    @Test
    fun `same bytes hash identically and different bytes differ`() {
        val a = byteArrayOf(1, 2, 3, 4)
        assertEquals(PhotoHash.sha256Hex(a), PhotoHash.sha256Hex(byteArrayOf(1, 2, 3, 4)))
        assertNotEquals(PhotoHash.sha256Hex(a), PhotoHash.sha256Hex(byteArrayOf(4, 3, 2, 1)))
    }

    @Test
    fun `output is lowercase hex of fixed length`() {
        val hex = PhotoHash.sha256Hex(byteArrayOf(-1, 0, 127))
        assertEquals(64, hex.length)
        assertEquals(hex, hex.lowercase())
    }
}
