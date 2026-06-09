package com.example.domain.model

enum class Badge(val minVerifiedContributions: Int, val labelAr: String) {
    REPORTER(5, "مراسل"),
    TRUSTED_REPORTER(25, "مراسل موثوق"),
    ELITE_REPORTER(100, "مراسل نخبة"),
    LEGEND(500, "أسطورة")
}
