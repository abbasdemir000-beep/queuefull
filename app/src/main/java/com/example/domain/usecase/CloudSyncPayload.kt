package com.example.domain.usecase

import com.example.domain.model.AppUser
import com.example.domain.model.City
import com.example.domain.model.QueueUpdate
import com.example.domain.model.Station
import java.util.Locale

/**
 * Pure builder for the Firebase RTDB sync payload.
 *
 * Replaces the previous string-interpolation JSON in the ViewModel, which
 * produced invalid (and injectable) JSON whenever a user/station name contained
 * a quote, backslash, or newline. Field set and structure are unchanged.
 */
object CloudSyncPayload {

    /** Escapes a string for safe embedding inside a JSON string literal. */
    fun escape(value: String): String = buildString(value.length) {
        for (ch in value) {
            when (ch) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else ->
                    if (ch < ' ') append(String.format(Locale.ROOT, "\\u%04x", ch.code))
                    else append(ch)
            }
        }
    }

    private fun str(value: String): String = "\"${escape(value)}\""

    fun build(
        timestamp: Long,
        users: List<AppUser>,
        stations: List<Station>,
        cities: List<City>,
        updates: List<QueueUpdate>
    ): String {
        val usersJson = users.joinToString(",") { u ->
            """{"phoneNumber":${str(u.phoneNumber)},"name":${str(u.name)},"role":${str(u.role)},"points":${u.points}}"""
        }
        val stationsJson = stations.joinToString(",") { s ->
            """{"id":${s.id},"name":${str(s.name)},"cityId":${s.cityId},"cityName":${str(s.cityName)},"type":${str(s.type)},"queueStatus":${str(s.queueStatus)},"hasFuel":${s.hasFuel},"isApproved":${s.isApproved}}"""
        }
        val citiesJson = cities.joinToString(",") { c ->
            """{"id":${c.id},"nameAr":${str(c.nameAr)},"nameEn":${str(c.nameEn)},"isApproved":${c.isApproved}}"""
        }
        val updatesJson = updates.joinToString(",") { q ->
            """{"id":${q.id},"stationId":${q.stationId},"userPhone":${str(q.userPhone)},"queueStatus":${str(q.queueStatus)},"fuelType":${str(q.fuelType)},"timestamp":${q.timestamp}}"""
        }
        return """{
  "sync_timestamp": $timestamp,
  "users": [$usersJson],
  "stations": [$stationsJson],
  "cities": [$citiesJson],
  "queue_updates": [$updatesJson]
}"""
    }

    /**
     * Normalizes an admin-entered database URL into the final sync endpoint.
     * Always upgrades to https — cleartext http is both unsafe and blocked by
     * the platform default (`usesCleartextTraffic=false` on modern targets).
     * Returns null when the input is blank.
     */
    fun buildEndpointUrl(databaseUrl: String, authToken: String): String? {
        var url = databaseUrl.trim()
        if (url.isBlank()) return null
        url = when {
            url.startsWith("https://") -> url
            url.startsWith("http://") -> "https://" + url.removePrefix("http://")
            else -> "https://$url"
        }
        if (!url.endsWith("/")) url += "/"
        url += "queue_fuel_data.json"
        if (authToken.isNotBlank()) url += "?auth=$authToken"
        return url
    }
}
