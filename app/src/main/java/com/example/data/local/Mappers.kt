package com.example.data.local

import com.example.domain.model.AdBanner
import com.example.domain.model.AppUser
import com.example.domain.model.City
import com.example.domain.model.QueueUpdate
import com.example.domain.model.Station

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
    banned = banned
)

fun AppUser.toEntity(): AppUserEntity = AppUserEntity(
    phoneNumber = phoneNumber,
    name = name,
    role = role,
    points = points,
    banned = banned
)
