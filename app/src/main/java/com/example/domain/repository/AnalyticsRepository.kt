package com.example.domain.repository

import com.example.domain.model.AnalyticsEvent

interface AnalyticsRepository {
    fun logEvent(event: AnalyticsEvent)
}
