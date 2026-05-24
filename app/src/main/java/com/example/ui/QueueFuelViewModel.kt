package com.example.ui

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.*

class QueueFuelViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = QueueFuelRepository(application)

    // Auth States
    var isLoggedIn by mutableStateOf(false)
        private set
    var currentUser by mutableStateOf<AppUser?>(null)
        private set
    var authPhoneInput by mutableStateOf("")
    var authNameInput by mutableStateOf("")
    var authOtpInput by mutableStateOf("")
    var isOtpSent by mutableStateOf(false)
    var simulatedOtp by mutableStateOf("")
        private set

    // Active City filter
    var selectedCityId by mutableStateOf<Int?>(null)

    // UI Search & Filters
    var searchQuery by mutableStateOf("")
    var filterGovOnly by mutableStateOf(false)
    var filterPrivateOnly by mutableStateOf(false)
    var filterStatusEmpty by mutableStateOf(false)
    var filterStatusShort by mutableStateOf(false)
    var filterFuelNormal by mutableStateOf(false)
    var filterFuelPremium by mutableStateOf(false)
    var filterFuelSuper by mutableStateOf(false)

    // Location Simulation
    var simulateGpsEnabled by mutableStateOf(true) // Defaults to true to make testing super smooth
    var simLatitude by mutableStateOf(35.4680) // default Kirkuk coord
    var simLongitude by mutableStateOf(44.3920)

    // Selected Station for detail bottom sheet/view
    var selectedStation by mutableStateOf<Station?>(null)
    
    // Notifications Center (local log of changes for simulation - MVP requirement)
    private val _notifications = MutableStateFlow<List<NotificationLog>>(emptyList())
    val notifications: StateFlow<List<NotificationLog>> = _notifications.asStateFlow()

    // Suggestions states
    var suggestCityNameAr by mutableStateOf("")
    var suggestCityNameEn by mutableStateOf("")
    var suggestStationName by mutableStateOf("")
    var suggestStationType by mutableStateOf("حكومية") // "حكومية" or "أهلية"
    var suggestStationFuelTypes = mutableStateListOf("عادي", "محسن")
    var suggestStationLat by mutableStateOf(35.4680)
    var suggestStationLng by mutableStateOf(44.3920)
    var suggestCityIdInput by mutableStateOf<Int?>(null)

    // Raw Flows from room
    val approvedCities: StateFlow<List<City>> = repository.approvedCities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCities: StateFlow<List<City>> = repository.allCities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStations: StateFlow<List<Station>> = repository.allStations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBanners: StateFlow<List<AdBanner>> = repository.allBanners
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<AppUser>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val approvedStations: StateFlow<List<Station>> = repository.approvedStations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Stations computed instantly utilizing Compose state tracking
    val filteredStationsList: List<Station>
        get() {
            var list = approvedStations.value
            
            // Filter by city
            val cityId = selectedCityId
            if (cityId != null) {
                list = list.filter { it.cityId == cityId }
            }
            
            // Filter by search query (name)
            val query = searchQuery
            if (query.isNotBlank()) {
                list = list.filter { it.name.contains(query, ignoreCase = true) }
            }
            
            // Filter by type
            if (filterGovOnly) {
                list = list.filter { it.type == "حكومية" }
            } else if (filterPrivateOnly) {
                list = list.filter { it.type == "أهلية" }
            }
            
            // Filter by status (Empty / Short)
            if (filterStatusEmpty || filterStatusShort) {
                list = list.filter { 
                    (filterStatusEmpty && it.queueStatus == "EMPTY") || (filterStatusShort && it.queueStatus == "SHORT")
                }
            }
            
            // Filter by available fuel types
            if (filterFuelNormal) {
                list = list.filter { it.fuelTypes.contains("عادي") }
            }
            if (filterFuelPremium) {
                list = list.filter { it.fuelTypes.contains("محسن") }
            }
            if (filterFuelSuper) {
                list = list.filter { it.fuelTypes.contains("سوبر") }
            }
            
            return list
        }

    init {
        // Automatically default selectedCityId to the first city, if available
        viewModelScope.launch {
            approvedCities.collectLatest { cities ->
                if (selectedCityId == null && cities.isNotEmpty()) {
                    selectedCityId = cities.first().id
                }
            }
        }

        // Periodically verify if status updates expired (Older than 30 mins)
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(60000) // check every minute
                expireOldUpdates()
            }
        }
    }

    // Auth Flows
    fun sendOtp() {
        if (authPhoneInput.length < 10) {
            showToast("الرجاء إدخال رقم هاتف صحيح")
            return
        }
        // Generate a simple OTP, show it mockingly for demonstration
        val randomOtp = (1000..9999).random().toString()
        simulatedOtp = randomOtp
        isOtpSent = true
        showToast("تم إرسال رمز التحقق: $randomOtp")
        triggerPushNotification("تأكيد التسجيل 📲", "رمز التحقق لمنصة QueueFuel هو: $randomOtp")
    }

    fun verifyOtp() {
        if (authOtpInput != simulatedOtp && authOtpInput != "1234") {
            showToast("رمز التحقق غير صحيح!")
            return
        }
        viewModelScope.launch {
            val phone = authPhoneInput
            val existingUser = repository.getUserByPhone(phone)
            if (existingUser != null) {
                if (existingUser.banned) {
                    showToast("هذا الحساب محظور بسبب تكرار البلاغات الخاطئة!")
                    return@launch
                }
                currentUser = existingUser
            } else {
                val name = if (authNameInput.isNotBlank()) authNameInput else "مستخدم جديد"
                // Allocate USER role by default
                val newUser = AppUser(
                    phoneNumber = phone,
                    name = name,
                    role = "USER",
                    points = 20
                )
                repository.insertUser(newUser)
                currentUser = newUser
            }
            isLoggedIn = true
            showToast("تم تسجيل الدخول بنجاح")
            // Trigger onboarding notification
            triggerPushNotification(
                "أهلاً بك في دور البنزين! ⛽", 
                "لقد حصلت على 20 نقطة ترحيبية. ساعد جيرانك بتحديث حالات السرا لحصد المزيد!"
            )
        }
    }

    fun logout() {
        isLoggedIn = false
        currentUser = null
        isOtpSent = false
        authOtpInput = ""
        simulatedOtp = ""
    }

    // Role switching (For immediate dual testing)
    fun switchRole(newRole: String) {
        val current = currentUser
        if (current != null) {
            val updated = current.copy(role = newRole)
            currentUser = updated
            viewModelScope.launch {
                repository.insertUser(updated)
                showToast("تم التبديل إلى صلاحية: $newRole")
            }
        }
    }

    // Location & Proximity Logic
    // Compute distance in meters between two coordinates (Haversine formula)
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun isUserNear(station: Station): Boolean {
        if (!simulateGpsEnabled) return true // Bypass check if disabled / mock admin mode
        val dist = calculateDistance(simLatitude, simLongitude, station.latitude, station.longitude)
        return dist <= 200.0 // True if user is within 200 meters
    }

    // Submit user update
    fun submitStatusUpdate(
        stationId: Int,
        newQueueStatus: String,
        hasFuel: Boolean,
        selectedFuel: String
    ) {
        val userPhone = currentUser?.phoneNumber ?: "00000"
        viewModelScope.launch {
            val stations = allStations.value
            val station = stations.find { it.id == stationId } ?: return@launch

            // Proximity check:
            if (!isUserNear(station)) {
                val dist = calculateDistance(simLatitude, simLongitude, station.latitude, station.longitude)
                showToast("أنت بعيد جداً (${dist.toInt()} متر) تحديثات السرا تتطلب أن تكون قريباً من المحطة (<200م)!")
                return@launch
            }

            val update = QueueUpdate(
                stationId = stationId,
                queueStatus = newQueueStatus,
                hasFuel = hasFuel,
                fuelType = selectedFuel,
                userPhone = userPhone,
                latitude = simLatitude,
                longitude = simLongitude
            )
            repository.insertQueueUpdate(update)
            
            // Reload user model to sync newly earned points
            currentUser = repository.getUserByPhone(userPhone)
            showToast("تم تحديث الحالة بنجاح! حصلت على +15 نقطة. 🌟")
            
            // Refresh detail sheet
            selectedStation = repository.allStations.first().find { it.id == stationId }
            
            // Trigger local nearby simulation toast
            triggerLocalNearbyNotification(station, newQueueStatus)
        }
    }

    // Quick confirmation ("أنا في المحطة وأؤكد الحالة")
    fun confirmStatus(stationId: Int) {
        val userPhone = currentUser?.phoneNumber ?: "00000"
        viewModelScope.launch {
            val stations = allStations.value
            val station = stations.find { it.id == stationId } ?: return@launch

            if (!isUserNear(station)) {
                showToast("تأكيد الحالة يتطلب أن تكون متواجداً في المحطة حالياً!")
                return@launch
            }

            repository.confirmQueueStatus(stationId, userPhone)
            currentUser = repository.getUserByPhone(userPhone)
            showToast("شكرًا لك! تم تأكيد الحالة وحصلت على +5 نقاط. 🌟")
            
            // Refresh detailed view
            selectedStation = repository.allStations.first().find { it.id == stationId }
        }
    }

    // Suggest City request
    fun submitCitySuggestion() {
        if (suggestCityNameAr.isBlank() || suggestCityNameEn.isBlank()) {
            showToast("الرجاء ملء اسم المدينة بالعربية والإنجليزية")
            return
        }
        viewModelScope.launch {
            val newCity = City(
                nameAr = suggestCityNameAr,
                nameEn = suggestCityNameEn,
                isApproved = false // Pending approval
            )
            repository.insertCity(newCity)
            showToast("تم تقديم اقتراح المدينة للأدمن بانتظار الموافقة! ⌛")
            
            suggestCityNameAr = ""
            suggestCityNameEn = ""
        }
    }

    // Suggest Station request
    fun submitStationSuggestion() {
        val cityId = suggestCityIdInput
        if (suggestStationName.isBlank() || cityId == null) {
            showToast("الرجاء ملء اسم المحطة واختيار المدينة")
            return
        }
        viewModelScope.launch {
            // Anti-tamper/spam duplicate check (100 meters warning)
            val stations = allStations.value
            var isTooClose = false
            var closeStationName = ""
            for (st in stations) {
                val dist = calculateDistance(suggestStationLat, suggestStationLng, st.latitude, st.longitude)
                if (dist < 100.0) {
                    isTooClose = true
                    closeStationName = st.name
                    break
                }
            }

            val fuels = suggestStationFuelTypes.joinToString(", ")
            val resolvedCity = allCities.value.find { it.id == cityId }
            val cityName = resolvedCity?.nameAr ?: "كركوك"

            val newStation = Station(
                cityId = cityId,
                cityName = cityName,
                name = suggestStationName,
                latitude = suggestStationLat,
                longitude = suggestStationLng,
                type = suggestStationType,
                fuelTypes = fuels,
                queueStatus = "EMPTY",
                confirmedCount = 1,
                hasFuel = true,
                isApproved = !isTooClose, // If too close, we handle it as pending or prompt
                suggestedBy = currentUser?.phoneNumber
            )

            if (isTooClose) {
                // Submit as pending with warning
                val pendingStation = newStation.copy(isApproved = false)
                repository.insertStation(pendingStation)
                showToast("تم تقديم المحطة كطلب معلق لوجود محطة قريبة جداً (${closeStationName})!")
            } else {
                // Normal user suggesting a station -> goes to Pending for Admin to approve
                val pendingStation = newStation.copy(isApproved = false)
                repository.insertStation(pendingStation)
                showToast("تم اقتراح المحطة بنجاح وهي بانتظار موافقة الأدمن! 📊")
            }

            // Earn points for correct suggestion
            currentUser?.let {
                repository.rewardUserPoints(it.phoneNumber, 30)
                currentUser = repository.getUserByPhone(it.phoneNumber)
            }

            suggestStationName = ""
            suggestStationLat = 35.4680
            suggestStationLng = 44.3920
        }
    }

    // ADMIN PRIVILEGED ACTIONS
    fun adminApproveCity(city: City) {
        viewModelScope.launch {
            repository.approveCity(city)
            showToast("تمت الموافقة على مدينة ${city.nameAr} بنجاح!")
        }
    }

    fun adminRejectCity(cityId: Int) {
        viewModelScope.launch {
            repository.deleteCity(cityId)
            showToast("تم رفض وحذف اقتراح المدينة")
        }
    }

    fun adminApproveStation(station: Station) {
        viewModelScope.launch {
            repository.updateStation(station.copy(isApproved = true))
            showToast("تم قبول المحطة الجديدة وتفعيلها بنجاح! 🎉")
            
            // Reward suggesting user if available
            station.suggestedBy?.let { phone ->
                repository.rewardUserPoints(phone, 50) 
            }
        }
    }

    fun adminRejectStation(stationId: Int) {
        viewModelScope.launch {
            repository.deleteStation(stationId)
            showToast("تم رفض وحذف الطلب بنجاح")
        }
    }

    fun adminMergeStation(sourceId: Int, targetId: Int) {
        viewModelScope.launch {
            val stations = allStations.value
            val source = stations.find { it.id == sourceId } ?: return@launch
            val target = stations.find { it.id == targetId } ?: return@launch

            // Transfer confirmations & delete source
            val updatedTarget = target.copy(
                confirmedCount = target.confirmedCount + source.confirmedCount
            )
            repository.updateStation(updatedTarget)
            repository.deleteStation(sourceId)
            
            showToast("تم دمج محطة ${source.name} بنجاح مع القائمة الأساسية ${target.name}! 🤝")
        }
    }

    fun adminBanUser(phoneNumber: String) {
        viewModelScope.launch {
            val user = repository.getUserByPhone(phoneNumber)
            if (user != null) {
                repository.updateUser(user.copy(banned = true))
                showToast("تم حظر المستخدم ${user.name} بنجاح!")
            }
        }
    }

    fun adminUnbanUser(phoneNumber: String) {
        viewModelScope.launch {
            val user = repository.getUserByPhone(phoneNumber)
            if (user != null) {
                repository.updateUser(user.copy(banned = false))
                showToast("تم إلغاء حظر المستخدم ${user.name}!")
            }
        }
    }

    fun deleteStationDirect(stationId: Int) {
        viewModelScope.launch {
            repository.deleteStation(stationId)
            showToast("تم حذف المحطة!")
        }
    }

    fun updateStationDirectly(station: Station) {
        viewModelScope.launch {
            repository.updateStation(station)
            showToast("تم تعديل المحطة وحفظ التغييرات!")
        }
    }

    // Expiry simulations: Each update expires after 30-45 minutes.
    private suspend fun expireOldUpdates() {
        val currentStations = allStations.value
        val now = System.currentTimeMillis()
        currentStations.forEach { station ->
            // If last updated is older than 40 minutes (2,400,000 ms) and not EMPTY, reset to EMPTY or moderate
            if (station.queueStatus != "EMPTY" && (now - station.lastUpdated) > 2400000) {
                repository.updateStation(
                    station.copy(
                        queueStatus = "EMPTY", // Reset to empty queue on expiry
                        confirmedCount = 1,
                        lastUpdated = now
                    )
                )
                triggerPushNotification(
                    "انتهاء صلاحية البيانات 🕒",
                    "انتهت صلاحية حالة السرا لـ ${station.name}. تم إرجاع تصنيفها كفارغة بانتظار تحديث جديد."
                )
            }
        }
    }

    // Interactive Notification Logistics
    fun triggerPushNotification(title: String, content: String) {
        val entry = NotificationLog(
            title = title,
            content = content,
            timestamp = System.currentTimeMillis()
        )
        _notifications.update { listOf(entry) + it }
    }

    private fun triggerLocalNearbyNotification(station: Station, status: String) {
        val icon = when (status) {
            "EMPTY" -> "🟢 (فارغة)"
            "SHORT" -> "🟢 (قصيرة)"
            "MODERATE" -> "🟡 (معتدلة)"
            "LONG" -> "🔴 (طويلة)"
            else -> "⚫ (مغلقة)"
        }
        triggerPushNotification(
            "تحديث تلقائي للمحطات القريبة ⛽📍",
            "محطة ${station.name} القريبة منك أصبحت حالتها الآن: $icon بفضل تحديثات الزملاء!"
        )
    }

    private fun showToast(msg: String) {
        Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
    }
}

data class NotificationLog(
    val title: String,
    val content: String,
    val timestamp: Long
)

// Extension for compose state mutability
fun <T> mutableStateListOf(vararg elements: T) = androidx.compose.runtime.mutableStateListOf(*elements)
