package com.example.domain.usecase

import java.security.MessageDigest

/**
 * Content hash for report photos, used by [AntiSpamPolicy.isDuplicatePhoto]
 * and stored on the server (`photoHashes/{hash}`) to reject re-submitted
 * pictures across devices.
 */
object PhotoHash {

    private val HEX = "0123456789abcdef".toCharArray()

    /** SHA-256 of the photo bytes as lowercase hex (locale-independent). */
    fun sha256Hex(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        val out = CharArray(digest.size * 2)
        digest.forEachIndexed { i, byte ->
            val v = byte.toInt() and 0xFF
            out[i * 2] = HEX[v ushr 4]
            out[i * 2 + 1] = HEX[v and 0x0F]
        }
        return String(out)
    }
}
