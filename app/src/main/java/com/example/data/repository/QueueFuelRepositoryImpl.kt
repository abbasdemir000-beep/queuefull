package com.example.data.repository

import android.content.Context
import com.example.data.local.AdBannerEntity
import com.example.data.local.AppUserEntity
import com.example.data.local.CityEntity
import com.example.data.local.QueueFuelDatabase
import com.example.data.local.ReportConfidenceEntity
import com.example.data.local.RewardEntryEntity
import com.example.data.local.StationEntity
import com.example.data.local.UserBadgeEntity
import com.example.data.local.toDomain
import com.example.data.local.toEntity
import com.example.domain.model.AdBanner
import com.example.domain.model.AppUser
import com.example.domain.model.City
import com.example.domain.model.LeaderboardEntry
import com.example.domain.model.QueueUpdate
import com.example.domain.model.ReportConfidence
import com.example.domain.model.RewardEntry
import com.example.domain.model.Station
import com.example.domain.model.UserBadge
import com.example.domain.repository.QueueFuelRepository
import com.example.domain.usecase.AuthPolicy
import com.example.domain.usecase.BadgePolicy
import com.example.domain.usecase.LeaderboardPolicy
import com.example.domain.usecase.ReportConfidencePolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QueueFuelRepositoryImpl(context: Context) : QueueFuelRepository {

    private val database: QueueFuelDatabase = QueueFuelDatabase.getInstance(context)

    private val dao = database.dao()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedDatabaseIfEmpty()
        }
    }

    override val approvedCities: Flow<List<City>> =
        dao.getApprovedCities().map { list -> list.map { it.toDomain() } }
    override val allCities: Flow<List<City>> =
        dao.getAllCities().map { list -> list.map { it.toDomain() } }
    override val approvedStations: Flow<List<Station>> =
        dao.getApprovedStations().map { list -> list.map { it.toDomain() } }
    override val allStations: Flow<List<Station>> =
        dao.getAllStations().map { list -> list.map { it.toDomain() } }
    override val allBanners: Flow<List<AdBanner>> =
        dao.getAllBanners().map { list -> list.map { it.toDomain() } }
    override val allUsers: Flow<List<AppUser>> =
        dao.getAllUsers().map { list -> list.map { it.toDomain() } }
    override val allQueueUpdates: Flow<List<QueueUpdate>> =
        dao.getAllQueueUpdates().map { list -> list.map { it.toDomain() } }

    override fun getStationsByCity(cityId: Int): Flow<List<Station>> =
        dao.getStationsByCity(cityId).map { list -> list.map { it.toDomain() } }

    override fun getBannersByCity(city: String): Flow<List<AdBanner>> =
        dao.getBannersByCity(city).map { list -> list.map { it.toDomain() } }

    override fun getUpdatesForStation(stationId: Int): Flow<List<QueueUpdate>> =
        dao.getUpdatesForStation(stationId).map { list -> list.map { it.toDomain() } }

    override suspend fun getLatestUpdateForStation(stationId: Int): QueueUpdate? =
        withContext(Dispatchers.IO) {
            dao.getLatestUpdateForStation(stationId)?.toDomain()
        }

    override suspend fun insertCity(city: City) = withContext(Dispatchers.IO) {
        dao.insertCity(city.toEntity())
        Unit
    }

    override suspend fun approveCity(city: City) = withContext(Dispatchers.IO) {
        dao.updateCity(city.copy(isApproved = true).toEntity())
    }

    override suspend fun deleteCity(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteCityById(id)
    }

    override suspend fun insertStation(station: Station) = withContext(Dispatchers.IO) {
        dao.insertStation(station.toEntity())
        Unit
    }

    override suspend fun updateStation(station: Station) = withContext(Dispatchers.IO) {
        dao.updateStation(station.toEntity())
    }

    override suspend fun deleteStation(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteStationById(id)
    }

    override suspend fun insertQueueUpdate(update: QueueUpdate) = withContext(Dispatchers.IO) {
        val rowId = dao.insertQueueUpdate(update.toEntity()).toInt()

        val stations = dao.getAllStations().first()
        val station = stations.find { it.id == update.stationId }
        if (station != null) {
            val confirmedCount = if (station.queueStatus == update.queueStatus) {
                station.confirmedCount + 1
            } else {
                1
            }
            dao.updateStation(
                station.copy(
                    queueStatus = update.queueStatus,
                    hasFuel = update.hasFuel,
                    confirmedCount = confirmedCount,
                    lastUpdated = System.currentTimeMillis()
                )
            )
            rewardUserPoints(update.userPhone, 15)

            // Upsert report confidence
            val now = System.currentTimeMillis()
            dao.insertReportConfidence(
                ReportConfidenceEntity(
                    stationId = update.stationId,
                    originalReporterId = update.userPhone,
                    confirmationCount = 0,
                    confidenceScore = ReportConfidencePolicy.INITIAL_SCORE,
                    timestamp = now,
                    expiresAt = ReportConfidencePolicy.computeExpiresAt(now),
                    isExpired = false
                )
            )

            // Check and award badges
            val user = dao.getUserByPhone(update.userPhone)
            if (user != null) {
                val badge = BadgePolicy.highestEarned(user.lifetimePoints)
                if (badge != null) {
                    val existing = dao.getBadgesForUser(update.userPhone)
                    if (existing.none { it.badge == badge.name }) {
                        dao.insertUserBadge(
                            UserBadgeEntity(userPhone = update.userPhone, badge = badge.name)
                        )
                    }
                }
            }
        }
    }

    override suspend fun confirmQueueStatus(stationId: Int, userPhone: String) =
        withContext(Dispatchers.IO) {
            val stations = dao.getAllStations().first()
            val station = stations.find { it.id == stationId }
            if (station != null) {
                dao.updateStation(
                    station.copy(
                        confirmedCount = station.confirmedCount + 1,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
                rewardUserPoints(userPhone, 5)

                // Update confidence score
                val existing = dao.getConfidenceForStation(stationId)
                if (existing != null) {
                    val ageMs = System.currentTimeMillis() - existing.timestamp
                    val newScore = ReportConfidencePolicy.calculateScore(
                        existing.confirmationCount + 1,
                        ageMs
                    )
                    dao.updateConfidence(stationId, newScore)
                }
            }
        }

    override suspend fun rewardUserPoints(phone: String, pointsToAdd: Int) {
        val user = dao.getUserByPhone(phone)
        if (user != null) {
            dao.updateUser(
                user.copy(
                    points = user.points + pointsToAdd,
                    lifetimePoints = user.lifetimePoints + pointsToAdd,
                    monthlyPoints = user.monthlyPoints + pointsToAdd
                )
            )
        }
    }

    override suspend fun insertBanner(banner: AdBanner) = withContext(Dispatchers.IO) {
        dao.insertBanner(banner.toEntity())
        Unit
    }

    override suspend fun deleteBanner(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteBannerById(id)
    }

    override suspend fun getUserByPhone(phone: String): AppUser? = withContext(Dispatchers.IO) {
        dao.getUserByPhone(phone)?.toDomain()
    }

    override suspend fun insertUser(user: AppUser) = withContext(Dispatchers.IO) {
        dao.insertUser(user.toEntity())
    }

    override suspend fun updateUser(user: AppUser) = withContext(Dispatchers.IO) {
        dao.updateUser(user.toEntity())
    }

    override suspend fun getLeaderboard(): List<LeaderboardEntry> = withContext(Dispatchers.IO) {
        val users = dao.getUsersRankedByMonthlyPoints().map { it.toDomain() }
        LeaderboardPolicy.rank(users)
    }

    override suspend fun resetMonthlyPoints() = withContext(Dispatchers.IO) {
        dao.resetAllMonthlyPoints()
    }

    override suspend fun getBadgesForUser(phone: String): List<UserBadge> =
        withContext(Dispatchers.IO) {
            dao.getBadgesForUser(phone).map { it.toDomain() }
        }

    override suspend fun awardBadge(badge: UserBadge) = withContext(Dispatchers.IO) {
        dao.insertUserBadge(badge.toEntity())
    }

    override suspend fun createRewardEntry(entry: RewardEntry) = withContext(Dispatchers.IO) {
        dao.insertRewardEntry(entry.toEntity())
        Unit
    }

    override suspend fun getRewardEntriesForMonth(monthYear: String): List<RewardEntry> =
        withContext(Dispatchers.IO) {
            dao.getRewardEntriesForMonth(monthYear).map { it.toDomain() }
        }

    override suspend fun markRewardEntryVerified(entryId: Int) = withContext(Dispatchers.IO) {
        dao.markRewardEntryVerified(entryId)
    }

    override suspend fun upsertReportConfidence(confidence: ReportConfidence) =
        withContext(Dispatchers.IO) {
            dao.insertReportConfidence(confidence.toEntity())
            Unit
        }

    override suspend fun getConfidenceForStation(stationId: Int): ReportConfidence? =
        withContext(Dispatchers.IO) {
            dao.getConfidenceForStation(stationId)?.toDomain()
        }

    override suspend fun confirmReportConfidence(stationId: Int, newScore: Float) =
        withContext(Dispatchers.IO) {
            dao.updateConfidence(stationId, newScore)
        }

    override suspend fun expireOldConfidences(now: Long) = withContext(Dispatchers.IO) {
        dao.expireOldConfidence(now)
    }

    private suspend fun seedDatabaseIfEmpty() {
        val cities = dao.getApprovedCities().first()
        if (cities.isEmpty()) {
            val kirkukId = dao.insertCity(CityEntity(nameAr = "كركوك", nameEn = "Kirkuk")).toInt()
            val erbilId = dao.insertCity(CityEntity(nameAr = "أربيل", nameEn = "Erbil")).toInt()
            val sulId = dao.insertCity(CityEntity(nameAr = "السليمانية", nameEn = "Sulaymaniyah")).toInt()

            dao.insertStation(StationEntity(cityId = kirkukId, cityName = "كركوك", name = "محطة كركوك الحكومية النموذجية", latitude = 35.4682, longitude = 44.3921, type = "حكومية", fuelTypes = "عادي, محسن", queueStatus = "SHORT", confirmedCount = 6, hasFuel = true))
            dao.insertStation(StationEntity(cityId = kirkukId, cityName = "كركوك", name = "محطة بابا كركر الأهلية", latitude = 35.4854, longitude = 44.4103, type = "أهلية", fuelTypes = "عادي, محسن, سوبر", queueStatus = "MODERATE", confirmedCount = 12, hasFuel = true))
            dao.insertStation(StationEntity(cityId = kirkukId, cityName = "كركوك", name = "محطة طريق بغداد الحكومية", latitude = 35.4411, longitude = 44.3752, type = "حكومية", fuelTypes = "عادي, محسن", queueStatus = "CLOSED", confirmedCount = 1, hasFuel = false))
            dao.insertStation(StationEntity(cityId = kirkukId, cityName = "كركوك", name = "محطة غرناطة الأهلية", latitude = 35.4590, longitude = 44.3980, type = "أهلية", fuelTypes = "محسن, سوبر", queueStatus = "EMPTY", confirmedCount = 3, hasFuel = true))
            dao.insertStation(StationEntity(cityId = erbilId, cityName = "أربيل", name = "محطة كولان الأهلية الحديثة", latitude = 36.1915, longitude = 44.0094, type = "أهلية", fuelTypes = "عادي, محسن, سوبر", queueStatus = "EMPTY", confirmedCount = 8, hasFuel = true))
            dao.insertStation(StationEntity(cityId = erbilId, cityName = "أربيل", name = "محطة أربيل المركزية الحكومية", latitude = 36.1851, longitude = 44.0201, type = "حكومية", fuelTypes = "عادي, محسن", queueStatus = "LONG", confirmedCount = 23, hasFuel = true))
            dao.insertStation(StationEntity(cityId = erbilId, cityName = "أربيل", name = "محطة طريق الموصل الأهلية", latitude = 36.1620, longitude = 43.9550, type = "أهلية", fuelTypes = "عادي, محسن", queueStatus = "CLOSED", confirmedCount = 2, hasFuel = false))
            dao.insertStation(StationEntity(cityId = sulId, cityName = "السليمانية", name = "محطة بختياري الأهلية الممتازة", latitude = 35.5601, longitude = 45.4208, type = "أهلية", fuelTypes = "عادي, محسن, سوبر", queueStatus = "MODERATE", confirmedCount = 7, hasFuel = true))
            dao.insertStation(StationEntity(cityId = sulId, cityName = "السليمانية", name = "محطة عقبة بن نافع الحكومية", latitude = 35.5452, longitude = 45.4101, type = "حكومية", fuelTypes = "عادي", queueStatus = "EMPTY", confirmedCount = 15, hasFuel = true))

            dao.insertBanner(AdBannerEntity(title = "شركة الرافدين لزيوت السيارات 🚘", description = "خصم 20% على تبديل زيت المحرك الأصلي مع فلتر مجاني لجميع منتظري سرا كركوك! اتصل الآن.", city = "كركوك", category = "زيوت سيارات", ctaPhone = "+9647701234567"))
            dao.insertBanner(AdBannerEntity(title = "كراج أربيل الحديث للصيانة 🛠️", description = "فحص كمبيوتر مجاني وصيانة وتعديل الهيدروليك. موقعنا قرب محطة كولان الأهلية.", city = "أربيل", category = "كراج صيانة", ctaPhone = "+9647501234567"))
            dao.insertBanner(AdBannerEntity(title = "مطعم كباب السرا السليمانية 🍢", description = "توصيل مجاني ومباشر إلى سيارتك أثناء انتظارك في السرا! اشتر وجبتين واحصل على كوكا مجانية.", city = "السليمانية", category = "مطاعم", ctaPhone = "+9647801234567"))
            dao.insertBanner(AdBannerEntity(title = "مركز الفارس لغسيل السيارات السريع 🧼", description = "تلييع وغسيل بخاري بأحدث الأجهزة. خصم 15% عند إظهار الكوبون من تطبيق QueueFuel.", city = "كركوك", category = "غسيل سيارات", ctaPhone = "+9647701234511"))

            dao.insertUser(AppUserEntity(phoneNumber = AuthPolicy.ADMIN_PHONE, name = "مدير المنصة (الأدمن)", role = "ADMIN", points = 250, lifetimePoints = 250))
            dao.insertUser(AppUserEntity(phoneNumber = "07712345678", name = "أحمد البلاغي (مراسل)", role = "REPORTER", points = 95, lifetimePoints = 95))
        }
    }
}
