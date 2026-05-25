package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QueueFuelRepository(context: Context) {
    
    private val database: QueueFuelDatabase = Room.databaseBuilder(
        context.applicationContext,
        QueueFuelDatabase::class.java,
        "queue_fuel.db"
    )
    .fallbackToDestructiveMigration()
    .build()

    private val dao = database.dao()

    init {
        // Seed database in background scope
        CoroutineScope(Dispatchers.IO).launch {
            seedDatabaseIfEmpty()
        }
    }

    // Exposure of Flows
    val approvedCities: Flow<List<City>> = dao.getApprovedCities()
    val allCities: Flow<List<City>> = dao.getAllCities()
    val approvedStations: Flow<List<Station>> = dao.getApprovedStations()
    val allStations: Flow<List<Station>> = dao.getAllStations()
    val allBanners: Flow<List<AdBanner>> = dao.getAllBanners()
    val allUsers: Flow<List<AppUser>> = dao.getAllUsers()
    val allQueueUpdates: Flow<List<QueueUpdate>> = dao.getAllQueueUpdates()

    fun getStationsByCity(cityId: Int): Flow<List<Station>> {
        return dao.getStationsByCity(cityId)
    }

    fun getBannersByCity(city: String): Flow<List<AdBanner>> {
        return dao.getBannersByCity(city)
    }

    fun getUpdatesForStation(stationId: Int): Flow<List<QueueUpdate>> {
        return dao.getUpdatesForStation(stationId)
    }

    suspend fun insertCity(city: City) = withContext(Dispatchers.IO) {
        dao.insertCity(city)
    }

    suspend fun approveCity(city: City) = withContext(Dispatchers.IO) {
        dao.updateCity(city.copy(isApproved = true))
    }

    suspend fun deleteCity(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteCityById(id)
    }

    suspend fun insertStation(station: Station) = withContext(Dispatchers.IO) {
        dao.insertStation(station)
    }

    suspend fun updateStation(station: Station) = withContext(Dispatchers.IO) {
        dao.updateStation(station)
    }

    suspend fun deleteStation(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteStationById(id)
    }

    suspend fun insertQueueUpdate(update: QueueUpdate) = withContext(Dispatchers.IO) {
        // First insert update
        dao.insertQueueUpdate(update)
        
        // Then update the station's status
        val stations = dao.getAllStations().first()
        val station = stations.find { it.id == update.stationId }
        if (station != null) {
            val confirmedCount = if (station.queueStatus == update.queueStatus) {
                station.confirmedCount + 1
            } else {
                1 // reset confirmations for new status
            }
            dao.updateStation(
                station.copy(
                    queueStatus = update.queueStatus,
                    hasFuel = update.hasFuel,
                    confirmedCount = confirmedCount,
                    lastUpdated = System.currentTimeMillis()
                )
            )
            
            // Reward points to user:
            rewardUserPoints(update.userPhone, 15)
        }
    }

    suspend fun confirmQueueStatus(stationId: Int, userPhone: String) = withContext(Dispatchers.IO) {
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
        }
    }

    suspend fun rewardUserPoints(phone: String, pointsToAdd: Int) {
        val user = dao.getUserByPhone(phone)
        if (user != null) {
            dao.updateUser(user.copy(points = user.points + pointsToAdd))
        }
    }

    suspend fun insertBanner(banner: AdBanner) = withContext(Dispatchers.IO) {
        dao.insertBanner(banner)
    }

    suspend fun deleteBanner(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteBannerById(id)
    }

    suspend fun getUserByPhone(phone: String): AppUser? = withContext(Dispatchers.IO) {
        dao.getUserByPhone(phone)
    }

    suspend fun insertUser(user: AppUser) = withContext(Dispatchers.IO) {
        dao.insertUser(user)
    }

    suspend fun updateUser(user: AppUser) = withContext(Dispatchers.IO) {
        dao.updateUser(user)
    }

    private suspend fun seedDatabaseIfEmpty() {
        val cities = dao.getApprovedCities().first()
        if (cities.isEmpty()) {
            // Insert standard cities
            val kirkukId = dao.insertCity(City(nameAr = "كركوك", nameEn = "Kirkuk")).toInt()
            val erbilId = dao.insertCity(City(nameAr = "أربيل", nameEn = "Erbil")).toInt()
            val sulId = dao.insertCity(City(nameAr = "السليمانية", nameEn = "Sulaymaniyah")).toInt()

            // Insert standard stations (Kirkuk)
            dao.insertStation(
                Station(
                    cityId = kirkukId,
                    cityName = "كركوك",
                    name = "محطة كركوك الحكومية النموذجية",
                    latitude = 35.4682,
                    longitude = 44.3921,
                    type = "حكومية",
                    fuelTypes = "عادي, محسن",
                    queueStatus = "SHORT",
                    confirmedCount = 6,
                    hasFuel = true
                )
            )
            dao.insertStation(
                Station(
                    cityId = kirkukId,
                    cityName = "كركوك",
                    name = "محطة بابا كركر الأهلية",
                    latitude = 35.4854,
                    longitude = 44.4103,
                    type = "أهلية",
                    fuelTypes = "عادي, محسن, سوبر",
                    queueStatus = "MODERATE",
                    confirmedCount = 12,
                    hasFuel = true
                )
            )
            dao.insertStation(
                Station(
                    cityId = kirkukId,
                    cityName = "كركوك",
                    name = "محطة طريق بغداد الحكومية",
                    latitude = 35.4411,
                    longitude = 44.3752,
                    type = "حكومية",
                    fuelTypes = "عادي, محسن",
                    queueStatus = "CLOSED",
                    confirmedCount = 1,
                    hasFuel = false
                )
            )
            dao.insertStation(
                Station(
                    cityId = kirkukId,
                    cityName = "كركوك",
                    name = "محطة غرناطة الأهلية",
                    latitude = 35.4590,
                    longitude = 44.3980,
                    type = "أهلية",
                    fuelTypes = "محسن, سوبر",
                    queueStatus = "EMPTY",
                    confirmedCount = 3,
                    hasFuel = true
                )
            )

            // Erbil
            dao.insertStation(
                Station(
                    cityId = erbilId,
                    cityName = "أربيل",
                    name = "محطة كولان الأهلية الحديثة",
                    latitude = 36.1915,
                    longitude = 44.0094,
                    type = "أهلية",
                    fuelTypes = "عادي, محسن, سوبر",
                    queueStatus = "EMPTY",
                    confirmedCount = 8,
                    hasFuel = true
                )
            )
            dao.insertStation(
                Station(
                    cityId = erbilId,
                    cityName = "أربيل",
                    name = "محطة أربيل المركزية الحكومية",
                    latitude = 36.1851,
                    longitude = 44.0201,
                    type = "حكومية",
                    fuelTypes = "عادي, محسن",
                    queueStatus = "LONG",
                    confirmedCount = 23,
                    hasFuel = true
                )
            )
            dao.insertStation(
                Station(
                    cityId = erbilId,
                    cityName = "أربيل",
                    name = "محطة طريق الموصل الأهلية",
                    latitude = 36.1620,
                    longitude = 43.9550,
                    type = "أهلية",
                    fuelTypes = "عادي, محسن",
                    queueStatus = "CLOSED",
                    confirmedCount = 2,
                    hasFuel = false
                )
            )

            // Sulaymaniyah
            dao.insertStation(
                Station(
                    cityId = sulId,
                    cityName = "السليمانية",
                    name = "محطة بختياري الأهلية الممتازة",
                    latitude = 35.5601,
                    longitude = 45.4208,
                    type = "أهلية",
                    fuelTypes = "عادي, محسن, سوبر",
                    queueStatus = "MODERATE",
                    confirmedCount = 7,
                    hasFuel = true
                )
            )
            dao.insertStation(
                Station(
                    cityId = sulId,
                    cityName = "السليمانية",
                    name = "محطة عقبة بن نافع الحكومية",
                    latitude = 35.5452,
                    longitude = 45.4101,
                    type = "حكومية",
                    fuelTypes = "عادي",
                    queueStatus = "EMPTY",
                    confirmedCount = 15,
                    hasFuel = true
                )
            )

            // Insert standard banners
            dao.insertBanner(
                AdBanner(
                    title = "شركة الرافدين لزيوت السيارات 🚘",
                    description = "خصم 20% على تبديل زيت المحرك الأصلي مع فلتر مجاني لجميع منتظري سرا كركوك! اتصل الآن.",
                    city = "كركوك",
                    category = "زيوت سيارات",
                    ctaPhone = "+9647701234567"
                )
            )
            dao.insertBanner(
                AdBanner(
                    title = "كراج أربيل الحديث للصيانة 🛠️",
                    description = "فحص كمبيوتر مجاني وصيانة وتعديل الهيدروليك. موقعنا قرب محطة كولان الأهلية.",
                    city = "أربيل",
                    category = "كراج صيانة",
                    ctaPhone = "+9647501234567"
                )
            )
            dao.insertBanner(
                AdBanner(
                    title = "مطعم كباب السرا السليمانية 🍢",
                    description = "توصيل مجاني ومباشر إلى سيارتك أثناء انتظارك في السرا! اشتر وجبتين واحصل على كوكا مجانية.",
                    city = "السليمانية",
                    category = "مطاعم",
                    ctaPhone = "+9647801234567"
                )
            )
            dao.insertBanner(
                AdBanner(
                    title = "مركز الفارس لغسيل السيارات السريع 🧼",
                    description = "تلييع وغسيل بخاري بأحدث الأجهزة. خصم 15% عند إظهار الكوبون من تطبيق QueueFuel.",
                    city = "كركوك",
                    category = "غسيل سيارات",
                    ctaPhone = "+9647701234511"
                )
            )

            // Insert default Admin user
            dao.insertUser(
                AppUser(
                    phoneNumber = "07774564334",
                    name = "مدير المنصة (الأدمن)",
                    role = "ADMIN",
                    points = 250
                )
            )
            // Insert standard Reporter
            dao.insertUser(
                AppUser(
                    phoneNumber = "07712345678",
                    name = "أحمد البلاغي (مراسل)",
                    role = "REPORTER",
                    points = 95
                )
            )
        }
    }
}
