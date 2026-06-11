package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateListOf
import com.example.data.location.RealLocationDataSource
import com.example.data.repository.QueueFuelRepositoryImpl
import com.example.domain.model.*
import com.example.domain.repository.QueueFuelRepository
import com.example.domain.usecase.AuthPolicy
import com.example.domain.usecase.CloudSyncPayload
import com.example.domain.usecase.CyclePolicy
import com.example.domain.usecase.LocationPolicy
import com.example.domain.usecase.GeoProximity
import com.example.domain.usecase.PointsPolicy
import com.example.domain.usecase.ReportVerificationRequest
import com.example.domain.usecase.ReportVerifier
import com.example.domain.usecase.StatusExpiryPolicy
import com.example.domain.usecase.StubReportVerifier
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull

class QueueFuelViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: QueueFuelRepository = QueueFuelRepositoryImpl(application)

    // AI verification for reports — MVP ships the deterministic stub; swap in a
    // real vision-model implementation of ReportVerifier when an API key exists.
    private val reportVerifier: ReportVerifier = StubReportVerifier()

    // Shared HTTP client for cloud sync (one instance per VM, with timeouts so a
    // dead network can't hang the sync coroutine indefinitely).
    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    // Toast event channel — UI collects and shows Toast. Keeps VM free of android.widget.Toast.
    private val _toastEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    // Auth States
    var isLoggedIn by mutableStateOf(false)
        private set
    var currentUser by mutableStateOf<AppUser?>(null)
        private set
    var authPhoneInput by mutableStateOf("")
    var authNameInput by mutableStateOf("")
    var authCityInput by mutableStateOf<Int?>(null)

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

    // Location Simulation (fallback when no real GPS fix is available)
    var simulateGpsEnabled by mutableStateOf(true) // Defaults to true to make testing super smooth
    var simLatitude by mutableStateOf(35.4680) // default Kirkuk coord
    var simLongitude by mutableStateOf(44.3920)

    // Real device location (FusedLocationProvider). A fresh fix takes priority
    // over the simulation everywhere (see LocationPolicy).
    var locationPermissionGranted by mutableStateOf(false)
        private set
    var realFix by mutableStateOf<LocationPolicy.RealFix?>(null)
        private set
    var isLocating by mutableStateOf(false)
        private set

    // Lazy so ViewModel construction (and Robolectric tests) never touch Play Services.
    private val realLocation by lazy { RealLocationDataSource(getApplication()) }

    /** The position every feature should use — real GPS wins over simulation. */
    fun effectiveCoordinates(): LocationPolicy.Coordinates =
        LocationPolicy.effectiveCoordinates(realFix, simLatitude, simLongitude, System.currentTimeMillis())

    val currentLatitude: Double get() = effectiveCoordinates().latitude
    val currentLongitude: Double get() = effectiveCoordinates().longitude
    val usingRealLocation: Boolean get() = effectiveCoordinates().isReal

    fun onLocationPermissionResult(granted: Boolean) {
        locationPermissionGranted = granted
        if (granted) {
            refreshRealLocation()
        } else {
            showToast("لم يتم منح إذن الموقع — سيستمر استخدام الموقع التجريبي من الخريطة.")
        }
    }

    fun refreshRealLocation() {
        if (!locationPermissionGranted || isLocating) return
        isLocating = true
        viewModelScope.launch {
            val fix = realLocation.currentFix()
            isLocating = false
            if (fix != null) {
                realFix = fix
                showToast("تم تحديد موقعك الحقيقي بنجاح! 📍")
            } else {
                showToast("تعذر تحديد الموقع — تأكد من تفعيل خدمة الموقع (GPS) في جهازك.")
            }
        }
    }

    // Selected Station for detail bottom sheet/view
    var selectedStation by mutableStateOf<Station?>(null)

    // Outcome of the last submitted report — observed by the report wizard's
    // final step to render the AI-verification result. Null while in flight.
    var lastReportResult by mutableStateOf<ReportFlowResult?>(null)
    
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

    val allQueueUpdates: StateFlow<List<QueueUpdate>> = repository.allQueueUpdates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 5-Hour Cycle state for scores/rewards (timing logic lives in CyclePolicy)
    var cycleStartTime by mutableStateOf(0L)
    var timeRemainingString by mutableStateOf("")
    var showFeedbackRewardDialog by mutableStateOf(false)

    // Firebase Cloud Sync Configuration
    var firebaseDatabaseUrl by mutableStateOf("https://queuefuel-default-rtdb.firebaseio.com/")
    var firebaseWebApiKey by mutableStateOf("")
    var firebaseSyncEnabled by mutableStateOf(false)
    var firebaseSyncStatus by mutableStateOf("وضع محلي (أوفلاين) 📴")
    var isSyncInProgress by mutableStateOf(false)

    private val prefs by lazy {
        getApplication<Application>().getSharedPreferences("queue_fuel_pref", android.content.Context.MODE_PRIVATE)
    }

    fun loadFirebaseConfig() {
        firebaseSyncEnabled = prefs.getBoolean("firebase_sync_enabled", false)
        firebaseDatabaseUrl = prefs.getString("firebase_db_url", "https://queuefuel-default-rtdb.firebaseio.com/") ?: "https://queuefuel-default-rtdb.firebaseio.com/"
        firebaseWebApiKey = prefs.getString("firebase_api_key", "") ?: ""
        if (firebaseSyncEnabled) {
            firebaseSyncStatus = "متصل بالسحابة 🟢"
        } else {
            firebaseSyncStatus = "وضع محلي (أوفلاين) 📴"
        }
    }

    private fun loadOrCreateCycle() {
        val stored = prefs.getLong("cycle_start_time", 0L)
        val now = System.currentTimeMillis()
        val expired = stored != 0L && CyclePolicy.isCycleComplete(stored, now)
        if (stored == 0L || expired) {
            prefs.edit().putLong("cycle_start_time", now).apply()
            prefs.edit().putBoolean("reward_notice_sent", false).apply()
            // Points are only reset when an actual cycle expired while the app
            // was closed — not on the very first launch.
            if (expired) {
                resetCycleAction()
            }
            cycleStartTime = now
        } else {
            cycleStartTime = stored
        }
    }

    fun resetCycleAction() {
        viewModelScope.launch {
            // Reset regular users' points (read from the repository directly so
            // this works even when no screen is collecting the user stream)
            repository.allUsers.first().forEach { u ->
                if (u.role != "ADMIN") {
                    repository.updateUser(u.copy(points = PointsPolicy.CYCLE_RESET_POINTS))
                }
            }
            if (currentUser != null && currentUser?.role != "ADMIN") {
                currentUser = currentUser?.copy(points = PointsPolicy.CYCLE_RESET_POINTS)
            }
            showToast("تمت إعادة تعيين الدورة وتصفير النقاط وبدء الحساب من جديد! 🔄🏁")
        }
    }

    private fun updateCycleTimer() {
        val now = System.currentTimeMillis()
        val elapsed = now - cycleStartTime
        val remaining = CyclePolicy.remainingMs(cycleStartTime, now)

        if (CyclePolicy.isCycleComplete(cycleStartTime, now)) {
            // Cycle finished! Reset and start new cycle
            cycleStartTime = now
            prefs.edit().putLong("cycle_start_time", now).apply()
            prefs.edit().putBoolean("reward_notice_sent", false).apply()
            
            viewModelScope.launch {
                // Reset points for non-admins
                repository.allUsers.first().forEach { u ->
                    if (u.role != "ADMIN") {
                        repository.updateUser(u.copy(points = PointsPolicy.CYCLE_RESET_POINTS))
                    }
                }
                if (currentUser != null && currentUser?.role != "ADMIN") {
                    currentUser = currentUser?.copy(points = PointsPolicy.CYCLE_RESET_POINTS)
                }
                triggerPushNotification(
                    "انتهاء دورة النقاط 🕒🏆",
                    "انتهت الدورة الحالية (5 ساعات). تم تصفير النقاط للمنافسين وفتح باب المنافسة لدورة جديدة!"
                )
            }
        } else {
            // Check if we should trigger the 4h 58m notification
            val isNoticeSent = prefs.getBoolean("reward_notice_sent", false)
            if (!isNoticeSent && CyclePolicy.shouldSendPreEndNotice(elapsed)) {
                prefs.edit().putBoolean("reward_notice_sent", true).apply()
                triggerPushNotification(
                    "انظر من حصل على أعلى النقاط وقدم تقارير! 📊",
                    "تنبيه: متبقي دقيقتان فقط على انتهاء الدورة! اعرض قائمة المشتركين ونقاطهم الحالية لتكريم الفائز."
                )
            }
        }

        // Format remaining time to hh:mm:ss for display
        timeRemainingString = CyclePolicy.formatRemaining(remaining)
    }

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
        loadFirebaseConfig()
        restoreSession()
        // Pick up a previously granted location permission and refresh the fix
        locationPermissionGranted = androidx.core.content.ContextCompat.checkSelfPermission(
            application,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (locationPermissionGranted) {
            refreshRealLocation()
        }
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

        // Initialize and start 5-hour countdown timer loop
        loadOrCreateCycle()
        viewModelScope.launch {
            while (true) {
                updateCycleTimer()
                kotlinx.coroutines.delay(1000) // update countdown every second
            }
        }

        // Reactive auto-sync to Firebase cloud on any local database changes (debounced)
        viewModelScope.launch {
            combine(allCities, allStations, allUsers, allQueueUpdates) { _, _, _, _ -> true }
                .collectLatest {
                    if (firebaseSyncEnabled) {
                        kotlinx.coroutines.delay(2000) // 2 second debounce to prevent rapid redundant calls
                        syncAllDataToFirebase()
                    }
                }
        }
    }

    // Auth Flow — simple MVP registration: name + phone + city, saved directly.
    // No OTP, no SMS, no Firebase Phone Auth. phoneVerified stays false.
    fun registerAndLogin() {
        if (!AuthPolicy.isValidName(authNameInput)) {
            showToast("الرجاء إدخال الاسم الكامل أولاً للتسجيل والبدء بحصد جوائز نقاط التقارير!")
            return
        }
        if (!AuthPolicy.isValidPhone(authPhoneInput)) {
            showToast("الرجاء إدخال رقم هاتف صحيح (أرقام فقط، 10 أرقام على الأقل)")
            return
        }
        val cityId = authCityInput
        if (cityId == null) {
            showToast("الرجاء اختيار مدينتك للمتابعة")
            return
        }
        viewModelScope.launch {
            val phone = authPhoneInput.trim()
            val cityName = approvedCities.value.find { it.id == cityId }?.nameAr ?: ""
            val existingUser = repository.getUserByPhone(phone)
            if (existingUser != null && AuthPolicy.isUserBanned(existingUser)) {
                showToast("هذا الحساب محظور بسبب تكرار البلاغات الخاطئة!")
                return@launch
            }
            val assignedRole = AuthPolicy.resolveRole(phone)
            val user = if (existingUser != null) {
                existingUser.copy(
                    name = authNameInput.trim(),
                    city = cityName,
                    role = assignedRole
                )
            } else {
                val assignedPoints = if (assignedRole == "ADMIN") PointsPolicy.ADMIN_INITIAL_POINTS else PointsPolicy.WELCOME_POINTS
                AppUser(
                    phoneNumber = phone,
                    name = authNameInput.trim(),
                    role = assignedRole,
                    points = assignedPoints,
                    city = cityName,
                    phoneVerified = false
                )
            }
            repository.insertUser(user)
            currentUser = user
            selectedCityId = cityId
            isLoggedIn = true
            prefs.edit().putString("logged_in_phone", phone).apply()
            showToast("تم تسجيل الدخول بنجاح")
            // Trigger onboarding notification
            triggerPushNotification(
                "أهلاً بك في دور البنزين! ⛽",
                "لقد حصلت على 20 نقطة ترحيبية. ساعد جيرانك بتحديث حالات السرا لحصد المزيد!"
            )
        }
    }

    private fun restoreSession() {
        viewModelScope.launch {
            val phone = prefs.getString("logged_in_phone", null) ?: return@launch
            val user = repository.getUserByPhone(phone)
            if (user != null && !AuthPolicy.isUserBanned(user)) {
                currentUser = user
                isLoggedIn = true
            }
        }
    }

    fun logout() {
        isLoggedIn = false
        currentUser = null
        prefs.edit().remove("logged_in_phone").apply()
    }

    // Role switching (dev testing only — ADMIN role gated to admin phone)
    fun switchRole(newRole: String) {
        val current = currentUser ?: return
        if (newRole == "ADMIN" && !AuthPolicy.isAdminPhone(current.phoneNumber)) return
        val updated = current.copy(role = newRole)
        currentUser = updated
        viewModelScope.launch {
            repository.insertUser(updated)
            showToast("تم التبديل إلى صلاحية: $newRole")
        }
    }

    // Location & Proximity Logic
    // Compute distance in meters between two coordinates (Haversine formula).
    // Delegates to the pure GeoProximity use-case; signature kept for existing callers (UI).
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        return GeoProximity.distanceMeters(lat1, lon1, lat2, lon2)
    }

    fun isUserNear(station: Station): Boolean {
        val coords = effectiveCoordinates()
        // The MVP bypass (simulation toggle off) only applies while no real
        // fix exists — a real GPS position always enforces the 200 m geofence.
        if (LocationPolicy.mayBypassGeofence(coords, simulateGpsEnabled)) return true
        return GeoProximity.isWithinRadius(
            coords.latitude, coords.longitude, station.latitude, station.longitude
        )
    }

    // Submit user report: GPS proximity + mandatory photo + AI verification.
    // Only verified reports change the station, award points, and enter the raffle.
    fun submitStatusUpdate(
        stationId: Int,
        newQueueStatus: String,
        hasFuel: Boolean,
        selectedFuel: String,
        photoPath: String?
    ) {
        val userPhone = currentUser?.phoneNumber ?: "00000"
        viewModelScope.launch {
            val stations = allStations.value
            val station = stations.find { it.id == stationId } ?: return@launch

            // 1. Proximity check:
            val coords = effectiveCoordinates()
            if (!isUserNear(station)) {
                val dist = calculateDistance(coords.latitude, coords.longitude, station.latitude, station.longitude)
                showToast("أنت بعيد جداً (${dist.toInt()} متر) تحديثات السرا تتطلب أن تكون قريباً من المحطة (<200م)!")
                lastReportResult = ReportFlowResult(verified = false, note = "أنت بعيد عن المحطة (${dist.toInt()} متر) — يتطلب التقرير التواجد ضمن 200م.")
                return@launch
            }

            // 2. Photo is mandatory:
            if (photoPath.isNullOrBlank()) {
                showToast("التقرير يتطلب صورة من المحطة! التقط صورة أو اخترها من المعرض 📷")
                lastReportResult = ReportFlowResult(verified = false, note = "التقرير يتطلب صورة من الموقع.")
                return@launch
            }

            // When the geofence was bypassed (simulated position with the
            // toggle off), treat the reporter as standing at the station so
            // verification doesn't reject the report; real fixes are used as-is.
            val bypassed = LocationPolicy.mayBypassGeofence(coords, simulateGpsEnabled)
            val reportLat = if (bypassed) station.latitude else coords.latitude
            val reportLng = if (bypassed) station.longitude else coords.longitude

            // 3. AI verification (stub for MVP — see ReportVerifier)
            val result = reportVerifier.verify(
                ReportVerificationRequest(
                    stationLatitude = station.latitude,
                    stationLongitude = station.longitude,
                    userLatitude = reportLat,
                    userLongitude = reportLng,
                    claimedStatus = newQueueStatus,
                    photoPath = photoPath
                )
            )

            val update = QueueUpdate(
                stationId = stationId,
                queueStatus = newQueueStatus,
                hasFuel = hasFuel,
                fuelType = selectedFuel,
                userPhone = userPhone,
                latitude = reportLat,
                longitude = reportLng,
                photoPath = photoPath,
                verification = result.verdict,
                verificationNote = result.reason
            )
            repository.insertQueueUpdate(update)

            if (result.isVerified) {
                // Reload user model to sync newly earned points
                currentUser = repository.getUserByPhone(userPhone)
                showToast("تم التحقق من تقريرك وقبوله! حصلت على +15 نقطة ودخلت سحب الجوائز 🎉")

                // Refresh detail sheet
                selectedStation = repository.allStations.first().find { it.id == stationId }

                // Trigger local nearby simulation toast
                triggerLocalNearbyNotification(station, newQueueStatus)
            } else {
                showToast("لم يجتز تقريرك التحقق الآلي: ${result.reason} ⚠️ تم حفظه لمراجعة الأدمن.")
            }
            lastReportResult = ReportFlowResult(verified = result.isVerified, note = result.reason)
        }
    }

    // ---- Report photo helpers (side effects live here, not in the UI) ----

    private fun reportPhotosDir(): java.io.File =
        java.io.File(getApplication<Application>().filesDir, "report_photos").apply { mkdirs() }

    /** Persists a camera-captured bitmap; returns the saved file path or null. */
    fun saveReportPhoto(bitmap: android.graphics.Bitmap): String? {
        return try {
            val file = java.io.File(reportPhotosDir(), "report_${System.currentTimeMillis()}.jpg")
            java.io.FileOutputStream(file).use { out ->
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, out)
            }
            file.absolutePath
        } catch (e: Exception) {
            showToast("تعذر حفظ الصورة، حاول مجدداً!")
            null
        }
    }

    /** Copies a gallery-picked image into app storage; returns the file path or null. */
    fun importReportPhoto(uri: android.net.Uri): String? {
        return try {
            val file = java.io.File(reportPhotosDir(), "report_${System.currentTimeMillis()}.jpg")
            val resolver = getApplication<Application>().contentResolver
            resolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { input.copyTo(it) }
            } ?: return null
            file.absolutePath
        } catch (e: Exception) {
            showToast("تعذر استيراد الصورة، حاول مجدداً!")
            null
        }
    }

    // ---- Admin report review actions ----

    fun adminApproveReport(reportId: Int) {
        viewModelScope.launch {
            repository.approveReport(reportId)
            showToast("تم اعتماد التقرير يدوياً ومنح المُبلّغ النقاط ودخول السحب! ✅")
        }
    }

    fun adminRejectReport(reportId: Int) {
        viewModelScope.launch {
            repository.rejectReport(reportId)
            showToast("تم رفض التقرير ولن يدخل صاحبه السحب.")
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
                repository.rewardUserPoints(it.phoneNumber, PointsPolicy.STATION_SUGGESTION_POINTS)
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
                repository.rewardUserPoints(phone, PointsPolicy.STATION_APPROVAL_POINTS)
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
        // Read from the repository directly: the StateFlow's value is empty
        // whenever no screen is subscribed, which would silently skip expiry.
        val currentStations = repository.allStations.first()
        val now = System.currentTimeMillis()
        currentStations.forEach { station ->
            // If last updated is older than 40 minutes (2,400,000 ms) and not EMPTY, reset to EMPTY or moderate
            if (StatusExpiryPolicy.isExpired(station.queueStatus, station.lastUpdated, now)) {
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

    fun saveFirebaseSettings(url: String, apiKey: String, enabled: Boolean) {
        firebaseDatabaseUrl = url
        firebaseWebApiKey = apiKey
        firebaseSyncEnabled = enabled
        firebaseSyncStatus = if (enabled) "متصل بالسحابة 🟢" else "وضع محلي (أوفلاين) 📴"
        
        prefs.edit()
            .putBoolean("firebase_sync_enabled", enabled)
            .putString("firebase_db_url", url)
            .putString("firebase_api_key", apiKey)
            .apply()

        showToast("تم حفظ إعدادات سحابة فايربيس بنجاح! 💾☁️")
        if (enabled) {
            syncAllDataToFirebase()
        }
    }

    fun syncAllDataToFirebase() {
        if (isSyncInProgress) return
        isSyncInProgress = true
        firebaseSyncStatus = "جاري الحفظ والرفع إلى سحابة فايربيس... ⚡"

        viewModelScope.launch {
            try {
                // Read directly from the repository so the sync payload is
                // complete even when no screen is collecting the StateFlows.
                val payload = CloudSyncPayload.build(
                    timestamp = System.currentTimeMillis(),
                    users = repository.allUsers.first().filter { it.role != "ADMIN" },
                    stations = repository.allStations.first(),
                    cities = repository.allCities.first(),
                    updates = repository.allQueueUpdates.first()
                )

                val url = CloudSyncPayload.buildEndpointUrl(firebaseDatabaseUrl, firebaseWebApiKey)
                if (url == null) {
                    firebaseSyncStatus = "خطأ: رابط فايربيس فارغ! 🔴"
                    isSyncInProgress = false
                    return@launch
                }

                val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                val body = payload.toRequestBody(mediaType)
                val request = Request.Builder()
                    .url(url)
                    .put(body)
                    .build()

                val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    httpClient.newCall(request).execute()
                }

                response.use {
                    if (it.isSuccessful) {
                        firebaseSyncStatus = "متصل سحابياً ومزامن بنجاح! 🟢"
                        showToast("تمت مزامنة كافة البيانات وصلاحيات الأدمن بنجاح مع الفايربيس! ☁️🏁")
                    } else {
                        firebaseSyncStatus = "فشل المزامنة: رمز الخطأ ${it.code} 🔴"
                        showToast("خطأ استجابة الفايربيس: ${it.code}. يرجى التحقق من القواعد!")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                firebaseSyncStatus = "فشل في الاتصال والرفع ⚠️"
                showToast("خطأ اتصال فايربيس: تأكد من الإملاء وتوفر الإنترنت!")
            } finally {
                isSyncInProgress = false
            }
        }
    }

    private fun showToast(msg: String) {
        _toastEvent.tryEmit(msg)
    }
}

data class NotificationLog(
    val title: String,
    val content: String,
    val timestamp: Long
)

/** UI-facing outcome of a submitted report (drives the wizard's result step). */
data class ReportFlowResult(
    val verified: Boolean,
    val note: String
)
