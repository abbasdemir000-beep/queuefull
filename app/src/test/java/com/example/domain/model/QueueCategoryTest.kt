package com.example.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QueueCategoryTest {

    @Test
    fun `fuel is the only live category in the MVP`() {
        val live = QueueCategory.entries.filter { !it.comingSoon }
        assertEquals(listOf(QueueCategory.FUEL), live)
    }

    @Test
    fun `home grid always has eight categories with arabic labels`() {
        assertEquals(8, QueueCategory.entries.size)
        assertTrue(QueueCategory.entries.all { it.labelAr.isNotBlank() })
    }
}
