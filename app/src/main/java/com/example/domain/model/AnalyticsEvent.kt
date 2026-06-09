package com.example.domain.model

sealed class AnalyticsEvent {
    object AppOpen : AnalyticsEvent()
    data class StationViewed(val stationId: Int) : AnalyticsEvent()
    data class ReportSubmitted(val stationId: Int) : AnalyticsEvent()
    data class AdViewedFuture(val adId: Int) : AnalyticsEvent()
    data class AdClickedFuture(val adId: Int) : AnalyticsEvent()
    data class ReportConfirmed(val stationId: Int) : AnalyticsEvent()
    object LeaderboardViewed : AnalyticsEvent()
    data class RewardEntryCreated(val userPhone: String) : AnalyticsEvent()
}
