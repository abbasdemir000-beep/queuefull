package com.example.data.local

import com.example.domain.model.AdBanner
import com.example.domain.model.AdCategory
import com.example.domain.model.Advertisement
import com.example.domain.model.AppUser
import com.example.domain.model.Badge
import com.example.domain.model.City
import com.example.domain.model.QueueUpdate
import com.example.domain.model.ReportConfidence
import com.example.domain.model.RewardEntry
import com.example.domain.model.Station
import com.example.domain.model.UserBadge

/**
 * Pure, lossless conversions between Room persistence entities and domain models.
 *
 * Field names/types match 1:1, so these are mechanical copies. They exist so the
 * data layer is the only place that knows about Room, while everything above the
 * repository deals exclusively in domain models.
 */

// ---- City ----
fun CityEntity.toDomain(): City = City(
    id = id,
    nameAr = nameAr,
    nameEn = nameEn,
    isApproved = isApproved
)

fun City.toEntity(): CityEntity = CityEntity(
    id = id,
    nameAr = nameAr,
    nameEn = nameEn,
    isApproved = isApproved
)

// ---- Station ----
fun StationEntity.toDomain(): Station = Station(
    id = id,
    cityId = cityId,
    cityName = cityName,
    name = name,
    latitude = latitude,
    longitude = longitude,
    type = type,
    fuelTypes = fuelTypes,
    queueStatus = queueStatus,
    lastUpdated = lastUpdated,
    confirmedCount = confirmedCount,
    hasFuel = hasFuel,
    isApproved = isApproved,
    suggestedBy = suggestedBy
)

fun Station.toEntity(): StationEntity = StationEntity(
    id = id,
    cityId = cityId,
    cityName = cityName,
    name = name,
    latitude = latitude,
    longitude = longitude,
    type = type,
    fuelTypes = fuelTypes,
    queueStatus = queueStatus,
    lastUpdated = lastUpdated,
    confirmedCount = confirmedCount,
    hasFuel = hasFuel,
    isApproved = isApproved,
    suggestedBy = suggestedBy
)

// ---- QueueUpdate ----
fun QueueUpdateEntity.toDomain(): QueueUpdate = QueueUpdate(
    id = id,
    stationId = stationId,
    queueStatus = queueStatus,
    hasFuel = hasFuel,
    fuelType = fuelType,
    timestamp = timestamp,
    userPhone = userPhone,
    latitude = latitude,
    longitude = longitude
)

fun QueueUpdate.toEntity(): QueueUpdateEntity = QueueUpdateEntity(
    id = id,
    stationId = stationId,
    queueStatus = queueStatus,
    hasFuel = hasFuel,
    fuelType = fuelType,
    timestamp = timestamp,
    userPhone = userPhone,
    latitude = latitude,
    longitude = longitude
)

// ---- AdBanner ----
fun AdBannerEntity.toDomain(): AdBanner = AdBanner(
    id = id,
    title = title,
    description = description,
    city = city,
    category = category,
    ctaPhone = ctaPhone,
    imageUrl = imageUrl
)

fun AdBanner.toEntity(): AdBannerEntity = AdBannerEntity(
    id = id,
    title = title,
    description = description,
    city = city,
    category = category,
    ctaPhone = ctaPhone,
    imageUrl = imageUrl
)

// ---- AppUser ----
fun AppUserEntity.toDomain(): AppUser = AppUser(
    phoneNumber = phoneNumber,
    name = name,
    role = role,
    points = points,
    banned = banned,
    lifetimePoints = lifetimePoints,
    monthlyPoints = monthlyPoints,
    accountType = accountType,
    premiumUntil = premiumUntil
)

fun AppUser.toEntity(): AppUserEntity = AppUserEntity(
    phoneNumber = phoneNumber,
    name = name,
    role = role,
    points = points,
    banned = banned,
    lifetimePoints = lifetimePoints,
    monthlyPoints = monthlyPoints,
    accountType = accountType,
    premiumUntil = premiumUntil
)

// ---- Advertisement ----
fun AdvertisementEntity.toDomain(): Advertisement = Advertisement(
    id = id,
    title = title,
    description = description,
    imageUrl = imageUrl,
    businessName = businessName,
    category = AdCategory.valueOf(category),
    targetCity = targetCity,
    isActive = isActive,
    startDate = startDate,
    endDate = endDate,
    clickCount = clickCount,
    viewCount = viewCount
)

fun Advertisement.toEntity(): AdvertisementEntity = AdvertisementEntity(
    id = id,
    title = title,
    description = description,
    imageUrl = imageUrl,
    businessName = businessName,
    category = category.name,
    targetCity = targetCity,
    isActive = isActive,
    startDate = startDate,
    endDate = endDate,
    clickCount = clickCount,
    viewCount = viewCount
)

// ---- UserBadge ----
fun UserBadgeEntity.toDomain(): UserBadge = UserBadge(
    id = id,
    userPhone = userPhone,
    badge = Badge.valueOf(badge),
    awardedAt = awardedAt
)

fun UserBadge.toEntity(): UserBadgeEntity = UserBadgeEntity(
    id = id,
    userPhone = userPhone,
    badge = badge.name,
    awardedAt = awardedAt
)

// ---- RewardEntry ----
fun RewardEntryEntity.toDomain(): RewardEntry = RewardEntry(
    id = id,
    userPhone = userPhone,
    stationId = stationId,
    reportId = reportId,
    monthYear = monthYear,
    isVerified = isVerified,
    createdAt = createdAt
)

fun RewardEntry.toEntity(): RewardEntryEntity = RewardEntryEntity(
    id = id,
    userPhone = userPhone,
    stationId = stationId,
    reportId = reportId,
    monthYear = monthYear,
    isVerified = isVerified,
    createdAt = createdAt
)

// ---- ReportConfidence ----
fun ReportConfidenceEntity.toDomain(): ReportConfidence = ReportConfidence(
    id = id,
    stationId = stationId,
    originalReporterId = originalReporterId,
    confirmationCount = confirmationCount,
    confidenceScore = confidenceScore,
    timestamp = timestamp,
    expiresAt = expiresAt,
    isExpired = isExpired
)

fun ReportConfidence.toEntity(): ReportConfidenceEntity = ReportConfidenceEntity(
    id = id,
    stationId = stationId,
    originalReporterId = originalReporterId,
    confirmationCount = confirmationCount,
    confidenceScore = confidenceScore,
    timestamp = timestamp,
    expiresAt = expiresAt,
    isExpired = isExpired
)
