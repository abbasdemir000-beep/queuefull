package com.example.domain.model

/**
 * Queue categories — QueueFuel is "one application for all queues".
 * Fuel is the only live category in the MVP; the rest are part of the
 * product roadmap and surface as "coming soon" in the UI.
 */
enum class QueueCategory(val labelAr: String, val comingSoon: Boolean) {
    FUEL("الوقود", false),
    GOVERNMENT("دوائر حكومية", true),
    HOSPITALS("مستشفيات", true),
    BANKS("مصارف", true),
    UNIVERSITIES("جامعات", true),
    PASSPORT("الجوازات", true),
    SERVICE_CENTERS("مراكز خدمة", true),
    MORE("المزيد", true),
}
