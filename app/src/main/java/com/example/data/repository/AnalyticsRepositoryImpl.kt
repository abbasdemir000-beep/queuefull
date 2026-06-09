package com.example.data.repository

import com.example.domain.model.AnalyticsEvent
import com.example.domain.repository.AnalyticsRepository

class AnalyticsRepositoryImpl : AnalyticsRepository {

    override fun logEvent(event: AnalyticsEvent) {
        // No-op stub. Wire to Firebase Analytics or a backend when ready.
        // e.g. FirebaseAnalytics.getInstance(context).logEvent(event.name, bundle)
    }
}
