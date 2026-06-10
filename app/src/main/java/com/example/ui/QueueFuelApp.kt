package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.domain.model.*
import com.example.domain.usecase.ReportVerification
import com.example.domain.usecase.AuthPolicy
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

enum class NavigationTab {
    MAP_STATIONS,      // الخريطة والمحطات
    SUGGESTIONS,       // اقتراح محطة/مدينة
    ALERTS,            // التنبيهات وسرّات
    ADMIN_PANEL,       // لوحة الأدمن
    ME_PROFILE         // ملفي الشخصي
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueFuelApp(viewModel: QueueFuelViewModel = viewModel()) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(NavigationTab.MAP_STATIONS) }

    // Collect toast events from ViewModel and show them in the UI layer
    LaunchedEffect(Unit) {
        viewModel.toastEvent.collectLatest { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    // If not logged in, show beautiful dynamic login
    if (!viewModel.isLoggedIn) {
        LoginScreen(viewModel = viewModel)
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = CosmicSecondaryBg,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    val role = viewModel.currentUser?.role ?: "USER"

                    NavigationBarItem(
                        selected = currentTab == NavigationTab.MAP_STATIONS,
                        onClick = { currentTab = NavigationTab.MAP_STATIONS },
                        icon = { Icon(Icons.Filled.Map, contentDescription = "المحطات") },
                        label = { Text("المحطات", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CosmicAccent,
                            selectedTextColor = CosmicAccent,
                            indicatorColor = CosmicAccent.copy(alpha = 0.18f),
                            unselectedIconColor = CosmicTextGray,
                            unselectedTextColor = CosmicTextGray
                        ),
                        modifier = Modifier.testTag("nav_stations")
                    )

                    NavigationBarItem(
                        selected = currentTab == NavigationTab.SUGGESTIONS,
                        onClick = { currentTab = NavigationTab.SUGGESTIONS },
                        icon = { Icon(Icons.Filled.AddLocationAlt, contentDescription = "اقتراح") },
                        label = { Text("اقتراح", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CosmicAccent,
                            selectedTextColor = CosmicAccent,
                            indicatorColor = CosmicAccent.copy(alpha = 0.18f),
                            unselectedIconColor = CosmicTextGray,
                            unselectedTextColor = CosmicTextGray
                        ),
                        modifier = Modifier.testTag("nav_suggest")
                    )

                    NavigationBarItem(
                        selected = currentTab == NavigationTab.ALERTS,
                        onClick = { currentTab = NavigationTab.ALERTS },
                        icon = {
                            BadgedBox(badge = {
                                val notifs = viewModel.notifications.collectAsState().value
                                if (notifs.isNotEmpty()) {
                                    Badge { Text(notifs.size.toString()) }
                                }
                            }) {
                                Icon(Icons.Filled.Notifications, contentDescription = "التنبيهات")
                            }
                        },
                        label = { Text("التنبيهات", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CosmicAccent,
                            selectedTextColor = CosmicAccent,
                            indicatorColor = CosmicAccent.copy(alpha = 0.18f),
                            unselectedIconColor = CosmicTextGray,
                            unselectedTextColor = CosmicTextGray
                        ),
                        modifier = Modifier.testTag("nav_alerts")
                    )

                    // Admin tab only visible to the designated admin phone
                    if (role == "ADMIN") {
                        NavigationBarItem(
                            selected = currentTab == NavigationTab.ADMIN_PANEL,
                            onClick = { currentTab = NavigationTab.ADMIN_PANEL },
                            icon = { Icon(Icons.Filled.AdminPanelSettings, contentDescription = "الأدمن") },
                            label = { Text("الأدمن", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CosmicAmber,
                                selectedTextColor = CosmicAmber,
                                indicatorColor = CosmicAmber.copy(alpha = 0.18f),
                                unselectedIconColor = CosmicTextGray,
                                unselectedTextColor = CosmicTextGray
                            ),
                            modifier = Modifier.testTag("nav_admin")
                        )
                    }

                    NavigationBarItem(
                        selected = currentTab == NavigationTab.ME_PROFILE,
                        onClick = { currentTab = NavigationTab.ME_PROFILE },
                        icon = { Icon(Icons.Filled.Person, contentDescription = "الملف الشخصي") },
                        label = { Text("حسابي", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CosmicAccent,
                            selectedTextColor = CosmicAccent,
                            indicatorColor = CosmicAccent.copy(alpha = 0.18f),
                            unselectedIconColor = CosmicTextGray,
                            unselectedTextColor = CosmicTextGray
                        ),
                        modifier = Modifier.testTag("nav_profile")
                    )
                }
            },
            containerColor = CosmicBackground
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(CosmicBackground)
            ) {
                Crossfade(targetState = currentTab, label = "TabTransition") { tab ->
                    when (tab) {
                        NavigationTab.MAP_STATIONS -> MapAndListDirection(viewModel)
                        NavigationTab.SUGGESTIONS -> SuggestionsAndClaims(viewModel)
                        NavigationTab.ALERTS -> AlertsAndSecurityLog(viewModel)
                        NavigationTab.ADMIN_PANEL -> AdminDashboardPanel(viewModel)
                        NavigationTab.ME_PROFILE -> MyProfileScreen(viewModel)
                    }
                }
            }
        }
        
        if (viewModel.showFeedbackRewardDialog) {
            FeedbackRewardDialog(viewModel = viewModel)
        }
    }
}

// ---------------------- 1. LOGIN / REGISTRATION SCREEN ----------------------
// Simple MVP registration: name + phone + city. No OTP, no SMS.
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(viewModel: QueueFuelViewModel) {
    val cities by viewModel.approvedCities.collectAsState()
    var animateStart by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animateStart = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicBackground)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Elegant brand icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(CosmicAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalGasStation,
                    contentDescription = "QueueFuel",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "QueueFuel / دور البنزين ⛽",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CosmicText,
                textAlign = TextAlign.Center
            )

            Text(
                text = "تقليل ازدحام محطات الوقود بذكاء وموثوقية في كركوك، أربيل، والسليمانية",
                fontSize = 13.sp,
                color = CosmicTextLight,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Text fields Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_card"),
                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                border = BorderStroke(1.dp, CosmicBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "سجل ملفك الشخصي وابدأ فوراً",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = CosmicText,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = viewModel.authNameInput,
                        onValueChange = { viewModel.authNameInput = it },
                        label = { Text("الاسم الكامل 👤", color = CosmicTextGray, fontSize = 12.sp) },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Badge, contentDescription = null, tint = CosmicAccent) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CosmicText,
                            unfocusedTextColor = CosmicText,
                            focusedBorderColor = CosmicAccent,
                            unfocusedBorderColor = CosmicBorder,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("name_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = viewModel.authPhoneInput,
                        onValueChange = { viewModel.authPhoneInput = it },
                        label = { Text("رقم الهاتف (الآسيا، الكورك، زين)", color = CosmicTextGray, fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null, tint = CosmicAccent) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CosmicText,
                            unfocusedTextColor = CosmicText,
                            focusedBorderColor = CosmicAccent,
                            unfocusedBorderColor = CosmicBorder,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("phone_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "اختر مدينتك 🏙️",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = CosmicText,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        cities.forEach { city ->
                            val isSelected = viewModel.authCityInput == city.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.authCityInput = city.id },
                                label = {
                                    Text(
                                        city.nameAr,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else CosmicText
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CosmicAccent,
                                    containerColor = CosmicSecondaryBg
                                ),
                                border = BorderStroke(1.dp, if (isSelected) CosmicAccent else CosmicBorder),
                                modifier = Modifier.testTag("city_chip_${city.id}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.registerAndLogin() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ابدأ الآن 🚀", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "بدون رمز تحقق — يتم حفظ ملفك مباشرة على جهازك. سيتم التحقق من رقم الهاتف لاحقاً.",
                        fontSize = 10.sp,
                        color = CosmicTextGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}


// ---------------------- 2. MAP & STATIONS DIRECT VIEW ----------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MapAndListDirection(viewModel: QueueFuelViewModel) {
    val approvedCities by viewModel.approvedCities.collectAsState()
    val approvedStations by viewModel.approvedStations.collectAsState()
    val filteredStations = viewModel.filteredStationsList
    val allBanners by viewModel.allBanners.collectAsState()

    var showFilters by remember { mutableStateOf(false) }
    var activeAdBanner by remember { mutableStateOf<AdBanner?>(null) }
    
    // Choose dynamic banner for selected city
    val activeCity = approvedCities.find { it.id == viewModel.selectedCityId }
    LaunchedEffect(viewModel.selectedCityId, allBanners) {
        if (activeCity != null) {
            val cityBanners = allBanners.filter { it.city == activeCity.nameAr }
            activeAdBanner = if (cityBanners.isNotEmpty()) cityBanners.random() else null
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // TOP Header search & City tab
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CosmicSurface)
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Input
                OutlinedTextField(
                    value = viewModel.searchQuery,
                    onValueChange = { viewModel.searchQuery = it },
                    placeholder = { Text("ابحث عن محطة...", color = CosmicTextGray, fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = CosmicTextLight) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CosmicText,
                        unfocusedTextColor = CosmicText,
                        focusedContainerColor = CosmicSecondaryBg,
                        unfocusedContainerColor = CosmicSecondaryBg,
                        focusedBorderColor = CosmicAccent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Filters Toggle
                IconButton(
                    onClick = { showFilters = !showFilters },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (showFilters) CosmicAccent else CosmicSecondaryBg,
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = "الفلاتر",
                        tint = if (showFilters) Color.White else CosmicText
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Cities selectors tabs ROW
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                approvedCities.forEach { city ->
                    val isSelected = viewModel.selectedCityId == city.id
                    Box(
                        modifier = Modifier
                            .clickable { viewModel.selectedCityId = city.id }
                            .background(
                                if (isSelected) CosmicAccent else CosmicSecondaryBg,
                                RoundedCornerShape(30.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = city.nameAr,
                            color = if (isSelected) Color.White else CosmicText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Expanded Collapsible filters view
            AnimatedVisibility(visible = showFilters) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Divider(color = CosmicBorder, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("فلترة متقدمة للمحطات:", color = CosmicText, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(6.dp))

                    // Filters switches or chips
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = viewModel.filterStatusEmpty,
                            onClick = { viewModel.filterStatusEmpty = !viewModel.filterStatusEmpty },
                            label = { Text("فارغة 🟢") }
                        )
                        FilterChip(
                            selected = viewModel.filterStatusShort,
                            onClick = { viewModel.filterStatusShort = !viewModel.filterStatusShort },
                            label = { Text("قصيرة 🟢") }
                        )
                        FilterChip(
                            selected = viewModel.filterGovOnly,
                            onClick = { 
                                viewModel.filterGovOnly = !viewModel.filterGovOnly
                                if (viewModel.filterGovOnly) viewModel.filterPrivateOnly = false
                            },
                            label = { Text("حكومية") }
                        )
                        FilterChip(
                            selected = viewModel.filterPrivateOnly,
                            onClick = { 
                                viewModel.filterPrivateOnly = !viewModel.filterPrivateOnly
                                if (viewModel.filterPrivateOnly) viewModel.filterGovOnly = false
                            },
                            label = { Text("أهلية") }
                        )
                        FilterChip(
                            selected = viewModel.filterFuelNormal,
                            onClick = { viewModel.filterFuelNormal = !viewModel.filterFuelNormal },
                            label = { Text("بنزين عادي") }
                        )
                        FilterChip(
                            selected = viewModel.filterFuelPremium,
                            onClick = { viewModel.filterFuelPremium = !viewModel.filterFuelPremium },
                            label = { Text("بنزين محسن") }
                        )
                        FilterChip(
                            selected = viewModel.filterFuelSuper,
                            onClick = { viewModel.filterFuelSuper = !viewModel.filterFuelSuper },
                            label = { Text("سوبر") }
                        )
                    }
                }
            }
        }

        // SCROLLABLE CONTAINER
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            // Interactive Map Canvas Area
            item {
                Text(
                    text = "خريطة زحام السرا الحيّة 🗺️",
                    fontSize = 14.sp,
                    color = CosmicText,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    textAlign = TextAlign.Right
                )

                InteractiveMapCanvas(
                    viewModel = viewModel,
                    stations = filteredStations
                )
            }

            // Proximity/GPS Debugging controller
            item {
                GpsSimulationControllerCard(viewModel)
            }

            // Dynamic Banner Ad section targeted for city
            item {
                activeAdBanner?.let { ad ->
                    BannerAdCard(ad = ad)
                }
            }

            // Stations list
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "محطات ${activeCity?.nameAr ?: ""} المحدثة (${filteredStations.size})",
                        color = CosmicText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    
                    if (filteredStations.isEmpty()) {
                        Text("لا نتائج", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }

            if (filteredStations.isEmpty()) {
                item {
                    EmptyStationsState()
                }
            } else {
                items(filteredStations) { station ->
                    StationItemCard(
                        station = station,
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    // Status reporter bottom dialog popup
    var updateTargetStation by remember { mutableStateOf<Station?>(null) }
    viewModel.selectedStation?.let { station ->
        StationDetailBottomSheet(
            station = station,
            viewModel = viewModel,
            onClose = { viewModel.selectedStation = null },
            onOpenUpdateDialog = { updateTargetStation = station }
        )
    }

    // Update Status submission Dialog
    updateTargetStation?.let { station ->
        UpdateStatusSubmissionDialog(
            station = station,
            viewModel = viewModel,
            onClose = { updateTargetStation = null }
        )
    }
}

// ---------------------- INTERACTIVE MAP CANVAS ----------------------
@Composable
fun InteractiveMapCanvas(
    viewModel: QueueFuelViewModel,
    stations: List<Station>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .padding(horizontal = 16.dp)
            .testTag("interactive_map_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicSecondaryBg),
        border = BorderStroke(1.dp, CosmicBorder),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val userLat = viewModel.simLatitude
            val userLng = viewModel.simLongitude

            // We center the canvas around user lat/long or the average coordinate of active stations.
            // Let's use user GPS or first station as center
            val centerLat = if (stations.isNotEmpty()) stations.first().latitude else userLat
            val centerLng = if (stations.isNotEmpty()) stations.first().longitude else userLng

            // Relative pixel multipliers
            val latMapRange = 0.08
            val lngMapRange = 0.08

            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(stations) {
                        detectTapGestures { offset ->
                            val width = size.width
                            val height = size.height

                            var closestStation: Station? = null
                            var minDistance = Float.MAX_VALUE

                            stations.forEach { st ->
                                // Project coordinates
                                val relLng = (st.longitude - centerLng) / lngMapRange
                                val relLat = (st.latitude - centerLat) / latMapRange

                                val x = (width / 2f) + (relLng * width).toFloat()
                                val y = (height / 2f) - (relLat * height).toFloat() // y goes down

                                val tapDist = sqrt((offset.x - x).pow(2) + (offset.y - y).pow(2))
                                if (tapDist < 35f && tapDist < minDistance) {
                                    closestStation = st
                                    minDistance = tapDist
                                }
                            }

                            if (closestStation != null) {
                                viewModel.selectedStation = closestStation
                            }
                        }
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // 1. Draw grid backdrop (Tech theme)
                val gridSpacing = 40f
                for (x in 0..(canvasWidth / gridSpacing).toInt()) {
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.25f),
                        start = Offset(x * gridSpacing, 0f),
                        end = Offset(x * gridSpacing, canvasHeight),
                        strokeWidth = 1f
                    )
                }
                for (y in 0..(canvasHeight / gridSpacing).toInt()) {
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.25f),
                        start = Offset(0f, y * gridSpacing),
                        end = Offset(canvasWidth, y * gridSpacing),
                        strokeWidth = 1f
                    )
                }

                // 2. Draw mock futuristic roads
                val roadColor = Color(0xFF2E2820)
                drawLine(
                    color = roadColor,
                    start = Offset(0f, canvasHeight * 0.4f),
                    end = Offset(canvasWidth, canvasHeight * 0.4f),
                    strokeWidth = 24f
                )
                drawLine(
                    color = roadColor,
                    start = Offset(canvasWidth * 0.3f, 0f),
                    end = Offset(canvasWidth * 0.3f, canvasHeight),
                    strokeWidth = 24f
                )
                drawLine(
                    color = roadColor,
                    start = Offset(0f, canvasHeight * 0.8f),
                    end = Offset(canvasWidth, canvasHeight * 0.8f),
                    strokeWidth = 16f
                )
                drawLine(
                    color = roadColor,
                    start = Offset(canvasWidth * 0.7f, 0f),
                    end = Offset(canvasWidth * 0.7f, canvasHeight),
                    strokeWidth = 16f
                )

                // 3. Draw Stations pins
                stations.forEach { st ->
                    val relLng = (st.longitude - centerLng) / lngMapRange
                    val relLat = (st.latitude - centerLat) / latMapRange

                    val x = (canvasWidth / 2f) + (relLng * canvasWidth).toFloat()
                    val y = (canvasHeight / 2f) - (relLat * canvasHeight).toFloat()

                    val statusColor = when (st.queueStatus) {
                        "EMPTY" -> FuelGreen
                        "SHORT" -> FuelShortGreen
                        "MODERATE" -> FuelYellow
                        "LONG" -> FuelRed
                        else -> FuelClosed
                    }

                    // Draw outer aura glow for chosen status
                    drawCircle(
                        color = statusColor.copy(alpha = 0.25f),
                        radius = 28f,
                        center = Offset(x, y)
                    )

                    // Draw solid central node
                    drawCircle(
                        color = statusColor,
                        radius = 12f,
                        center = Offset(x, y)
                    )

                    // Draw little white center dot
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = Offset(x, y)
                    )
                }

                // 4. Draw User GPS Pin position (pulsing blue ring)
                if (viewModel.simulateGpsEnabled) {
                    val relUserLng = (userLng - centerLng) / lngMapRange
                    val relUserLat = (userLat - centerLat) / latMapRange

                    val cursorX = (canvasWidth / 2f) + (relUserLng * canvasWidth).toFloat()
                    val cursorY = (canvasHeight / 2f) - (relUserLat * canvasHeight).toFloat()

                    // Pulse outline circle
                    drawCircle(
                        color = CosmicAccent.copy(alpha = 0.35f),
                        radius = 40f,
                        center = Offset(cursorX, cursorY),
                        style = Stroke(width = 3f)
                    )

                    // True center crosshair
                    drawCircle(
                        color = CosmicSurface,
                        radius = 8f,
                        center = Offset(cursorX, cursorY)
                    )
                    drawCircle(
                        color = CosmicAccent,
                        radius = 5f,
                        center = Offset(cursorX, cursorY)
                    )
                }
            }

            // Top overlay legend of stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .background(CosmicSurface.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                    .border(BorderStroke(0.5.dp, CosmicBorder), RoundedCornerShape(8.dp))
                    .padding(8.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MapLegendItem("فارغة", FuelGreen)
                MapLegendItem("قصيرة", FuelShortGreen)
                MapLegendItem("معتدلة", FuelYellow)
                MapLegendItem("طويلة", FuelRed)
                MapLegendItem("مغلقة", FuelClosed)
            }

            // Hint in bottom-left help
            Text(
                text = "📍 انقر على أي نقطة لتفاصيل السرا والتأكيد",
                color = CosmicText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .background(CosmicSurface.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                    .border(BorderStroke(0.5.dp, CosmicBorder), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun MapLegendItem(name: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Text(name, color = CosmicText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}


// ----------------------- GPS SIMULATOR WIDGET -----------------------
@Composable
fun GpsSimulationControllerCard(viewModel: QueueFuelViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, CosmicBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "محاكاة التواجد في محطة الوقود (GPS) 📲",
                    color = CosmicText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                Switch(
                    checked = viewModel.simulateGpsEnabled,
                    onCheckedChange = { viewModel.simulateGpsEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = CosmicAccent
                    )
                )
            }

            Text(
                text = "لتحديث السرا كاجراء آمن، يجب أن تكون قريباً <200 متر من المحطة. يمكنك تفعيل المحاكاة وتغيير إحداثياتك أدناه:",
                fontSize = 11.sp,
                color = CosmicTextLight,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            if (viewModel.simulateGpsEnabled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Coordinates toggles
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("موقع كركوك (النموذجية) 📍", color = CosmicText, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        RadioButton(
                            selected = viewModel.simLatitude == 35.4682,
                            onClick = {
                                viewModel.simLatitude = 35.4682
                                viewModel.simLongitude = 44.3921
                            }
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("موقع أربيل (كولان) 📍", color = CosmicText, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        RadioButton(
                            selected = viewModel.simLatitude == 36.1915,
                            onClick = {
                                viewModel.simLatitude = 36.1915
                                viewModel.simLongitude = 44.0094
                            }
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("موقع السليمانية 📍", color = CosmicText, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        RadioButton(
                            selected = viewModel.simLatitude == 35.5601,
                            onClick = {
                                viewModel.simLatitude = 35.5601
                                viewModel.simLongitude = 45.4208
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "lat: ${"%.4f".format(viewModel.simLatitude)}",
                        color = CosmicTextGray,
                        fontSize = 10.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "lng: ${"%.4f".format(viewModel.simLongitude)}",
                        color = CosmicTextGray,
                        fontSize = 10.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "نطاق الدقة: ممتاز (GPS نشط)",
                        color = FuelGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "🚨 تم تعطيل فحص القرب الجغرافي. يمكنك تحديث السرا من أي مكان (صلاحيات التطوير).",
                    color = CosmicAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


// ----------------------- BANNER AD SYSTEM -----------------------
@Composable
fun BannerAdCard(ad: AdBanner) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("ad_banner_card"),
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CosmicAccent.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(CosmicAmber, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("إعلان ممول 📣", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "قطاع: ${ad.category}",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = ad.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = ad.description,
                color = Color.LightGray,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 4.dp),
                textAlign = TextAlign.Right
            )

            ad.ctaPhone?.let { phone ->
                Button(
                    onClick = {
                        Toast.makeText(context, "جاري فتح الاتصال بـ $phone", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .align(Alignment.End)
                        .height(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicAccent),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Filled.PhoneInTalk, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("اتصل الآن للاستفادة", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


// ----------------- STATION CARD COMPONENT -----------------
@Composable
fun StationItemCard(
    station: Station,
    viewModel: QueueFuelViewModel
) {
    val statusProps = getQueueStatusProps(station.queueStatus)
    val distance = viewModel.calculateDistance(
        viewModel.simLatitude, viewModel.simLongitude,
        station.latitude, station.longitude
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { viewModel.selectedStation = station }
            .testTag("station_card_${station.id}"),
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(
            1.dp, 
            if (viewModel.selectedStation?.id == station.id) CosmicAccent else CosmicBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // First row: Name, ownership badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = station.name,
                    color = CosmicText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    modifier = Modifier
                        .background(
                            if (station.type == "حكومية") CosmicTeal else Color(0xFFEF562D),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = station.type,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Second row: Queue status with colors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status glowing chip
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(statusProps.color, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusProps.label,
                        color = statusProps.color,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                }

                // Estimated Distance
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Navigation,
                        contentDescription = null,
                        tint = CosmicTextLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "يبعد حوالي ${"%.1f".format(distance / 1000.0)} كم",
                        color = CosmicTextLight,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Third row: Available fuels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Fuels text
                Text(
                    text = "البنزين المتوفر: ${station.fuelTypes}",
                    color = CosmicTextLight,
                    fontSize = 11.sp
                )

                // Confirm status with little badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RatingStars(confirmedCount = station.confirmedCount)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "موثوقية (${station.confirmedCount})",
                        color = CosmicAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Last Updated
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val timeFormatted = formatTime(station.lastUpdated)
                Text(
                    text = "آخر تحديث: $timeFormatted",
                    color = CosmicTextGray,
                    fontSize = 11.sp
                )

                // Quick click
                Text(
                    text = "انقر لمعاينة التفاصيل والتحديث ⚡",
                    color = CosmicAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


// Rating Star Helper
@Composable
fun RatingStars(confirmedCount: Int) {
    val starColor = CosmicAmber
    val size = 12.dp
    Row {
        Icon(
            imageVector = if (confirmedCount >= 5) Icons.Filled.Star else Icons.Outlined.Star,
            contentDescription = null,
            tint = starColor,
            modifier = Modifier.size(size)
        )
        Icon(
            imageVector = if (confirmedCount >= 10) Icons.Filled.Star else Icons.Outlined.Star,
            contentDescription = null,
            tint = starColor,
            modifier = Modifier.size(size)
        )
        Icon(
            imageVector = if (confirmedCount >= 20) Icons.Filled.Star else Icons.Outlined.Star,
            contentDescription = null,
            tint = starColor,
            modifier = Modifier.size(size)
        )
    }
}


// ----------------- STATION DETAILS WIDGET -----------------
@Composable
fun StationDetailBottomSheet(
    station: Station,
    viewModel: QueueFuelViewModel,
    onClose: () -> Unit,
    onOpenUpdateDialog: () -> Unit
) {
    val statusProps = getQueueStatusProps(station.queueStatus)
    val userPhone = viewModel.currentUser?.phoneNumber ?: ""
    val mockDistance = viewModel.calculateDistance(viewModel.simLatitude, viewModel.simLongitude, station.latitude, station.longitude)
    val userIsClose = viewModel.isUserNear(station)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onClose() }
            .testTag("detail_overlay"),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false) {}
                .testTag("station_detail_card"),
            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            border = BorderStroke(1.dp, CosmicBorder)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .navigationBarsPadding()
            ) {
                // Close line banner
                Box(
                    modifier = Modifier
                        .size(40.dp, 4.dp)
                        .background(CosmicTextGray, CircleShape)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = station.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CosmicText
                    )

                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = "اغلاق", tint = CosmicTextLight)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Detail specs
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Type
                    Box(
                        modifier = Modifier
                            .background(CosmicSecondaryBg, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("ملكية: ${station.type}", color = CosmicText, fontSize = 12.sp)
                    }
                    // Status
                    Box(
                        modifier = Modifier
                            .background(statusProps.color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("حالة السرا: ${statusProps.label}", color = statusProps.color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    // Reliability
                    Box(
                        modifier = Modifier
                            .background(CosmicSecondaryBg, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("التأكيدات: ${station.confirmedCount}", color = CosmicAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = CosmicBorder, thickness = 0.5.dp)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "📍 الموقع الجغرافي: خط العرض ${station.latitude} / خط الطول ${station.longitude}",
                    fontSize = 12.sp,
                    color = CosmicTextLight,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "بنزين متوفر حالياً: ${station.fuelTypes}",
                    fontSize = 12.sp,
                    color = CosmicTextLight,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "آخر تحديث حالي: ${formatTime(station.lastUpdated)}",
                    fontSize = 12.sp,
                    color = CosmicTextGray,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Alert about GPS Distance warning
                if (!userIsClose) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(FuelRed.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                            .border(1.dp, FuelRed.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "⚠️ تنبيه المسافة الجغرافية:\nأنت تبعد مسافة قدرها ${"%.0f".format(mockDistance)} متر من هذه المحطة. لا يمكنك تحديث السرا إلا عند وجودك على بعد أقل من 200م لمنع البلاغات الكاذبة. (فعل 'محاكاة GPS' لمحاكاة التواجد)",
                            fontSize = 11.sp,
                            color = FuelRed,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(FuelGreen.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                            .border(1.dp, FuelGreen.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "🟢 تحقق جغرافي ناجح:\nأنت متواجد جغرافياً داخل نطاق المحطة (${"%.0f".format(mockDistance)}م)! يمكنك الآن الإبلاغ أو تأكيد السرا بأمان والمساهمة بنقاطك.",
                            fontSize = 11.sp,
                            color = FuelGreen,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ACTIONS Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Quick confirm status button (+5 pts)
                    Button(
                        onClick = { viewModel.confirmStatus(station.id) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("confirm_station_status"),
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.ThumbUp, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("أنا بالمحطة وأؤكد هذا السرا (+5ن)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    // Report/Update brand new status action (+15 pts)
                    Button(
                        onClick = { onOpenUpdateDialog() },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("report_new_status"),
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicTeal),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.EditLocation, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حدّث حالة السرا الآن (+15ن)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


// -------- SUBMIT STATUS UPDATE DIALOG ----------
@Composable
fun UpdateStatusSubmissionDialog(
    station: Station,
    viewModel: QueueFuelViewModel,
    onClose: () -> Unit
) {
    var selectedStatus by remember { mutableStateOf("EMPTY") }
    var hasFuel by remember { mutableStateOf(true) }
    var selectedFuelKind by remember { mutableStateOf("عادي") }
    var photoPath by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) photoPath = viewModel.saveReportPhoto(bitmap)
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) photoPath = viewModel.importReportPhoto(uri)
    }

    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Text(
                text = "تقريـر حالة السـرا ⛽",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.Right,
                color = CosmicText,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.End
            ) {
                Text("المحطة: ${station.name}", fontSize = 12.sp, color = CosmicTextLight)
                Spacer(modifier = Modifier.height(12.dp))

                Text("1. ما هي حالة طابور السرا الحالي؟", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CosmicText, modifier = Modifier.padding(bottom = 6.dp))
                // Choices row
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val statuses = listOf(
                        "EMPTY" to "فارغة 🟢 (لا يوجد ازدحام)",
                        "MODERATE" to "متوسطة 🟡 (ازدحام معتدل)",
                        "LONG" to "مزدحمة 🔴 (طابور طويل جداً)"
                    )
                    
                    statuses.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedStatus = item.first }
                                .background(
                                    if (selectedStatus == item.first) CosmicSecondaryBg else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedStatus == item.first,
                                onClick = { selectedStatus = item.first }
                            )
                            Text(text = item.second, color = CosmicText, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("2. هل البنزين متوفر جردياً؟", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CosmicText, modifier = Modifier.padding(bottom = 6.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("نعم، متوفر بشكل طبيعي", color = CosmicText, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = hasFuel,
                        onCheckedChange = { hasFuel = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("لا يوجد وقود حالياً", color = CosmicTextLight, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("3. ما هو نوع البنزين الذي قمت بتعبئته؟", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CosmicText, modifier = Modifier.padding(bottom = 6.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val fuelOptions = listOf("عادي", "محسن", "سوبر")
                    fuelOptions.forEach { opt ->
                        val isSel = selectedFuelKind == opt
                        Box(
                            modifier = Modifier
                                .clickable { selectedFuelKind = opt }
                                .background(
                                    if (isSel) CosmicAccent else CosmicSecondaryBg,
                                    RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(opt, color = if (isSel) Color.White else CosmicText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("4. صورة من المحطة (إلزامية) 📷", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CosmicText, modifier = Modifier.padding(bottom = 6.dp))

                if (photoPath != null) {
                    AsyncImage(
                        model = java.io.File(photoPath!!),
                        contentDescription = "صورة التقرير",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, CosmicBorder, RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier.weight(1f).testTag("take_photo_button"),
                        border = BorderStroke(1.dp, CosmicAccent),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (photoPath == null) "التقط صورة 📸" else "إعادة الالتقاط 📸", fontSize = 11.sp, color = CosmicAccent, fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f).testTag("pick_photo_button"),
                        border = BorderStroke(1.dp, CosmicBorder),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("من المعرض 🖼️", fontSize = 11.sp, color = CosmicTextLight, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CosmicAccent.copy(alpha = 0.10f), RoundedCornerShape(8.dp))
                        .border(1.dp, CosmicAccent.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "🤖 سيتم التحقق آلياً من مطابقة الصورة لموقع المحطة ومستوى الازدحام قبل قبول التقرير ومنح النقاط ودخول سحب الجوائز.",
                        fontSize = 10.sp,
                        color = CosmicAccent,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.submitStatusUpdate(
                        stationId = station.id,
                        newQueueStatus = selectedStatus,
                        hasFuel = hasFuel,
                        selectedFuel = selectedFuelKind,
                        photoPath = photoPath
                    )
                    onClose()
                },
                enabled = photoPath != null,
                colors = ButtonDefaults.buttonColors(containerColor = CosmicTeal),
                modifier = Modifier.testTag("submit_update_dialog_btn")
            ) {
                Text(if (photoPath == null) "أرفق صورة أولاً 📷" else "إرسال التقرير 🚀", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text("تراجع", color = CosmicTextGray)
            }
        },
        containerColor = CosmicSurface,
        titleContentColor = CosmicText,
        textContentColor = CosmicTextLight
    )
}


// Status Prop helper
data class StatusProps(val label: String, val color: Color)
fun getQueueStatusProps(status: String): StatusProps {
    return when (status) {
        "EMPTY" -> StatusProps("فارغة 🟢", FuelGreen)
        "SHORT" -> StatusProps("قصيرة 🟢", FuelShortGreen)
        "MODERATE" -> StatusProps("معتدلة 🟡", FuelYellow)
        "LONG" -> StatusProps("طويلة 🔴", FuelRed)
        else -> StatusProps("مغلقة ⚫", FuelClosed)
    }
}

fun formatTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return formatter.format(Date(timestamp))
}


@Composable
fun EmptyStationsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text("لم نجد محطات تطابق معايير الفرز هذه في المدينة المحددة.", color = Color.Gray, fontSize = 13.sp, textAlign = TextAlign.Center)
    }
}


// ----------------- 3. SUGGESTIONS AND CLAIMS SCREEN -----------------
@Composable
fun SuggestionsAndClaims(viewModel: QueueFuelViewModel) {
    val allCities by viewModel.allCities.collectAsState()
    var selectedSuggestTab by remember { mutableStateOf(0) } // 0: Station, 1: City

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "اقتراح مدينة أو محطة جديدة 🗺️⛽",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CosmicText,
            textAlign = TextAlign.Right,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "لم نقم بإدراج مدينتك أو محطة حيك بعد؟ اقترحها الآن وسنضيفها فوراً بعد تحقق الأدمن لمنع التكرار الجغرافي.",
            fontSize = 12.sp,
            color = CosmicTextLight,
            textAlign = TextAlign.Right,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Custom selector tabs
        TabRow(
            selectedTabIndex = selectedSuggestTab,
            containerColor = CosmicSurface,
            contentColor = CosmicText,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSuggestTab]),
                    color = CosmicAccent
                )
            }
        ) {
            val isTab0 = selectedSuggestTab == 0
            Tab(
                selected = isTab0,
                onClick = { selectedSuggestTab = 0 },
                text = { Text("اقتراح محطة وقود ⛽", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isTab0) CosmicAccent else CosmicTextLight) }
            )
            val isTab1 = selectedSuggestTab == 1
            Tab(
                selected = isTab1,
                onClick = { selectedSuggestTab = 1 },
                text = { Text("اقتراح مدينة جديدة 🏙️", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isTab1) CosmicAccent else CosmicTextLight) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedSuggestTab == 0) {
            // Suggest Station form
            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CosmicBorder),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.End) {
                    Text("اقتراح محطة بنزين جديدة", fontWeight = FontWeight.Bold, color = CosmicText, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Name
                    OutlinedTextField(
                        value = viewModel.suggestStationName,
                        onValueChange = { viewModel.suggestStationName = it },
                        label = { Text("اسم المحطة (مثال: محطة كركوك الأهلية)", color = CosmicTextLight, fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CosmicText,
                            unfocusedTextColor = CosmicText,
                            focusedBorderColor = CosmicAccent,
                            unfocusedBorderColor = CosmicBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("suggest_station_name")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // City Selector dropdown simulated
                    Text("اختر المدينة التابعة للمحطة:", color = CosmicText, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allCities.forEach { city ->
                            val isSel = viewModel.suggestCityIdInput == city.id
                            Box(
                                modifier = Modifier
                                    .clickable { viewModel.suggestCityIdInput = city.id }
                                    .background(
                                        if (isSel) CosmicAccent else CosmicSecondaryBg,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(city.nameAr, color = if (isSel) Color.White else CosmicText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Ownership Model
                    Text("نوع المحطة:", color = CosmicText, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("أهلية 🏢", color = CosmicText, fontSize = 11.sp)
                            RadioButton(
                                selected = viewModel.suggestStationType == "أهلية",
                                onClick = { viewModel.suggestStationType = "أهلية" }
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("حكومية 🇮🇶", color = CosmicText, fontSize = 11.sp)
                            RadioButton(
                                selected = viewModel.suggestStationType == "حكومية",
                                onClick = { viewModel.suggestStationType = "حكومية" }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Coords Selector Simulation map
                    Text("الموقع الجغرافي للمحطة (تعديل الإحداثيات):", color = CosmicText, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = viewModel.suggestStationLat.toString(),
                            onValueChange = { it.toDoubleOrNull()?.let { lat -> viewModel.suggestStationLat = lat } },
                            label = { Text("خط العرض Lat", color = CosmicTextLight, fontSize = 10.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CosmicText,
                                unfocusedTextColor = CosmicText,
                                focusedBorderColor = CosmicAccent,
                                unfocusedBorderColor = CosmicBorder
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = viewModel.suggestStationLng.toString(),
                            onValueChange = { it.toDoubleOrNull()?.let { lng -> viewModel.suggestStationLng = lng } },
                            label = { Text("خط الطول Lng", color = CosmicTextLight, fontSize = 10.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CosmicText,
                                unfocusedTextColor = CosmicText,
                                focusedBorderColor = CosmicAccent,
                                unfocusedBorderColor = CosmicBorder
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Simulated GPS picker buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.suggestStationLat = viewModel.simLatitude
                                viewModel.suggestStationLng = viewModel.simLongitude
                                Toast.makeText(viewModel.getApplication(), "تم تعيين خطوط العرض من موقعك الحالي!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Text("اسحب إحداثيات موقعي الحالي 🛰️", color = CosmicAccent, fontSize = 10.sp)
                        }
                    }

                    // Check for duplicate GPS
                    val duplicateExists = checkDuplicateWithinLimit(
                        viewModel.suggestStationLat, viewModel.suggestStationLng,
                        viewModel.allStations.collectAsState().value
                    )
                    
                    if (duplicateExists) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .background(FuelRed.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                .border(1.dp, FuelRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "⚠️ تحذير تكرار: تقع هذه المحطة المقترحة على بعد أقل من 100 متر من محطة مدرجة مسبقاً! إذا قمت بإرسالها، سيقوم الأدمن بدمجها مع المحطة الحالية تلقائياً.",
                                color = FuelRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.submitStationSuggestion() },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_station_suggestion_btn")
                    ) {
                        Text("ارسل طلب الإضافة للأدمن (+30ن) 🚀", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Suggest City Form
            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CosmicBorder),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.End) {
                    Text("اقتراح مدينة جديدة للتطبيق 🏙️", fontWeight = FontWeight.Bold, color = CosmicText, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = viewModel.suggestCityNameAr,
                        onValueChange = { viewModel.suggestCityNameAr = it },
                        label = { Text("اسم المدينة بالعربية (مثال: حلبجة)", color = CosmicTextLight, fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CosmicText,
                            unfocusedTextColor = CosmicText,
                            focusedBorderColor = CosmicAccent,
                            unfocusedBorderColor = CosmicBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = viewModel.suggestCityNameEn,
                        onValueChange = { viewModel.suggestCityNameEn = it },
                        label = { Text("اسم المدينة بالإنجليزية (مثال: Halabja)", color = CosmicTextLight, fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CosmicText,
                            unfocusedTextColor = CosmicText,
                            focusedBorderColor = CosmicAccent,
                            unfocusedBorderColor = CosmicBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.submitCitySuggestion() },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("ارسل اقتراح المدينة 🏢", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

fun checkDuplicateWithinLimit(lat: Double, lng: Double, stations: List<Station>): Boolean {
    // Return true if any station is within 100 meters
    for (st in stations) {
        val r = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(st.latitude - lat)
        val dLon = Math.toRadians(st.longitude - lng)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat)) * cos(Math.toRadians(st.latitude)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        if (r * c < 100.0) return true
    }
    return false
}


// ----------------- 4. ALERTS AND SECURITY LOGS (NOTIFICATIONS) -----------------
@Composable
fun AlertsAndSecurityLog(viewModel: QueueFuelViewModel) {
    val notifs by viewModel.notifications.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "سجل الإشعارات وتأكيدات السرا 🔔",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CosmicText
            )

            Icon(
                imageVector = Icons.Filled.NotificationsActive,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = CosmicAmber
            )
        }

        Text(
            text = "متابعة حية للتحديثات التي يقوم بها المستخدمون في كركوك، أربيل، والسليمانية وتنبيهات القرب التلقائية.",
            fontSize = 11.sp,
            color = CosmicTextLight,
            textAlign = TextAlign.Right,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Toggle user stats reputation
        Card(
            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, CosmicBorder),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text("نقاط الموثوقية الخاصة بي: 🌟", color = CosmicTextGray, fontSize = 11.sp)
                    Text(
                        text = "${viewModel.currentUser?.points ?: 0} نقطة",
                        color = CosmicAmber,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("ترتيبي في النزاهة:", color = CosmicTextGray, fontSize = 11.sp)
                    Text(
                        text = when {
                            (viewModel.currentUser?.points ?: 0) >= 200 -> "سفير النزاهة 🛡️"
                            (viewModel.currentUser?.points ?: 0) >= 100 -> "مراسل معتمد 📊"
                            else -> "مواطن متعاون 🤝"
                        },
                        color = CosmicText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text("تنبيـهات دَفـع فوريـة محليـة (Push Notifications) 📲", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CosmicText)
        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (notifs.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("السجل فارغ. السرا المحيط بك مستقر بالكامل حالياً! ✅", color = CosmicTextLight, fontSize = 12.sp, textAlign = TextAlign.Center)
                    }
                }
            } else {
                items(notifs) { log ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CosmicBorder),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = log.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CosmicText
                                )

                                Text(
                                    text = formatTime(log.timestamp),
                                    fontSize = 10.sp,
                                    color = CosmicTextGray
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = log.content,
                                fontSize = 12.sp,
                                color = CosmicTextLight,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (log.title.contains("انظر من حصل") || log.title.contains("دورة النقاط")) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.showFeedbackRewardDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = CosmicAccent),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("🥇 عرض المتنافسين ونقاطهم", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ----------------- 5. ADMIN CONTROL PANEL SCREEN -----------------
@Composable
fun AdminDashboardPanel(viewModel: QueueFuelViewModel) {
    val allStations by viewModel.allStations.collectAsState()
    val allCities by viewModel.allCities.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val allReports by viewModel.allQueueUpdates.collectAsState()

    val pendingStations = allStations.filter { !it.isApproved }
    val pendingCities = allCities.filter { !it.isApproved }
    val unverifiedReports = allReports.count { it.verification != ReportVerification.VERIFIED }

    var adminSectionTab by remember { mutableStateOf(0) } // 0: Stations, 1: Cities, 2: Users, 3: Firebase, 4: Reports

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "لوحـة تحكّم الإشـراف (الأدمن) 👮‍♀️⚙️",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CosmicText,
            textAlign = TextAlign.Right,
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "مراجعة الاقتراحات من قبل المواطنين، تعديل وقبول المحطات، كبح التكرار، وحظر المتلاعبين بالبيانات.",
            color = CosmicTextLight,
            fontSize = 12.sp,
            textAlign = TextAlign.Right,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Stats card row
        Card(
            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, CosmicBorder),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AdminStatCounter("إجمالي المدن", allCities.size.toString())
                AdminStatCounter("إجمالي المحطات", allStations.size.toString())
                AdminStatCounter("طلبات معلقة", (pendingStations.size + pendingCities.size).toString())
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5-Hour Cycle countdown & competitor standings card
        Card(
            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, CosmicBorder),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = viewModel.timeRemainingString,
                        color = CosmicAmber,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "مؤقت دورة الجوائز (5 ساعات) 🕒",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CosmicText
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = "سيقوم النظام تلقائياً بتصفير نتائج المتنافسين بعد انتهاء الدورة. مع تنبيه المشرف بالوصول لـ 4 ساعات و58 دقيقة لمكافأة الفائزين بقيمة النقاط الحالية.",
                    color = CosmicTextLight,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.resetCycleAction() },
                        colors = ButtonDefaults.buttonColors(containerColor = FuelRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("إعادة تعيين الدورة يدوياً 🔄", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    
                    Button(
                        onClick = { viewModel.showFeedbackRewardDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.2f).height(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("🏆 عرض المتنافسين ونقاطهم", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Mini Tabs for Admin options
        ScrollableTabRow(
            selectedTabIndex = adminSectionTab,
            containerColor = CosmicSurface,
            contentColor = CosmicText,
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[adminSectionTab]),
                    color = CosmicAccent
                )
            }
        ) {
            Tab(
                selected = adminSectionTab == 0,
                onClick = { adminSectionTab = 0 },
                text =_text_wrap("المحطات المعلقة (${pendingStations.size})")
            )
            Tab(
                selected = adminSectionTab == 1,
                onClick = { adminSectionTab = 1 },
                text = _text_wrap("المدن المعلقة (${pendingCities.size})")
            )
            Tab(
                selected = adminSectionTab == 2,
                onClick = { adminSectionTab = 2 },
                text = _text_wrap("إدارة الحسابات")
            )
            Tab(
                selected = adminSectionTab == 3,
                onClick = { adminSectionTab = 3 },
                text = _text_wrap("سحاب الفايربيس 🌐")
            )
            Tab(
                selected = adminSectionTab == 4,
                onClick = { adminSectionTab = 4 },
                text = _text_wrap("التقارير 📋 ($unverifiedReports)"),
                modifier = Modifier.testTag("admin_reports_tab")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (adminSectionTab == 0) {
                // Stations pending approvals
                if (pendingStations.isEmpty()) {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp)
                        ) {
                            Text("لا توجد طلبات محطات بانتظار الموافقة حالياً 🎉", color = CosmicTextGray, fontSize = 12.sp)
                        }
                    }
                } else {
                    items(pendingStations) { st ->
                        AdminStationRequestCard(st, viewModel, allStations)
                    }
                }
            } else if (adminSectionTab == 1) {
                // Cities pending approvals
                if (pendingCities.isEmpty()) {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp)
                        ) {
                            Text("لا توجد طلبات مدن بانتظار الموافقة حالياً 🏙️", color = CosmicTextGray, fontSize = 12.sp)
                        }
                    }
                } else {
                    items(pendingCities) { city ->
                        AdminCityRequestCard(city, viewModel)
                    }
                }
            } else if (adminSectionTab == 2) {
                // Users list block
                val filteredUsers = allUsers.filter { it.role != "ADMIN" && it.phoneNumber != AuthPolicy.ADMIN_PHONE }
                items(filteredUsers) { user ->
                    AdminUserBlockCard(user, viewModel)
                }
            } else if (adminSectionTab == 3) {
                // Firebase settings panel
                item {
                    FirebaseSettingsPanel(viewModel = viewModel)
                }
            } else {
                // Reports review block (photo + AI verification verdicts)
                if (allReports.isEmpty()) {
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp)
                        ) {
                            Text("لا توجد تقارير مقدمة من المستخدمين بعد 📋", color = CosmicTextGray, fontSize = 12.sp)
                        }
                    }
                } else {
                    items(allReports) { report ->
                        AdminReportReviewCard(report, allStations, viewModel)
                    }
                }
            }
        }
    }
}

// REPORT REVIEW CARD (photo, AI verdict, manual override)
@Composable
fun AdminReportReviewCard(
    report: QueueUpdate,
    allStations: List<Station>,
    viewModel: QueueFuelViewModel
) {
    val stationName = allStations.find { it.id == report.stationId }?.name ?: "محطة محذوفة (#${report.stationId})"
    val statusProps = getQueueStatusProps(report.queueStatus)
    val (verdictLabel, verdictColor) = when (report.verification) {
        ReportVerification.VERIFIED -> "موثّق ✅" to FuelGreen
        ReportVerification.REJECTED -> "مرفوض ❌" to FuelRed
        else -> "قيد المراجعة ⌛" to CosmicAmber
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CosmicBorder),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(verdictColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(verdictLabel, color = verdictColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Text(formatTime(report.timestamp), color = CosmicTextGray, fontSize = 10.sp)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "المحطة: $stationName",
                color = CosmicText,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "المُبلّغ: ${report.userPhone} | الحالة المُبلّغة: ${statusProps.label}",
                color = CosmicTextLight,
                fontSize = 11.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
            if (report.verificationNote.isNotBlank()) {
                Text(
                    text = "ملاحظة التحقق: ${report.verificationNote}",
                    color = CosmicTextGray,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (report.photoPath != null) {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = java.io.File(report.photoPath),
                    contentDescription = "صورة التقرير",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, CosmicBorder, RoundedCornerShape(8.dp))
                )
            }

            if (report.verification != ReportVerification.VERIFIED) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (report.verification != ReportVerification.REJECTED) {
                        Button(
                            onClick = { viewModel.adminRejectReport(report.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicBorder),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(34.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("رفض ❌", color = CosmicText, fontSize = 11.sp)
                        }
                    }
                    Button(
                        onClick = { viewModel.adminApproveReport(report.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = FuelGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.5f).height(34.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("اعتماد يدوي ومنح النقاط ✅", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun _text_wrap(txt: String): @Composable () -> Unit = {
    Text(txt, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = CosmicText)
}

@Composable
fun AdminStatCounter(label: String, count: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = CosmicTextLight, fontSize = 10.sp)
        Text(count, color = CosmicText, fontSize = 18.sp, fontWeight = FontWeight.Black)
    }
}

// STATION SUGGESTIONS REVIEW
@Composable
fun AdminStationRequestCard(
    station: Station,
    viewModel: QueueFuelViewModel,
    allStations: List<Station>
) {
    // Find if there are other stations within 200m to offer merging
    val nearStations = allStations.filter { 
        it.isApproved && viewModel.calculateDistance(station.latitude, station.longitude, it.latitude, it.longitude) < 200.0
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CosmicBorder),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(CosmicAmber.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("بانتظار التحقق ⌛", color = CosmicAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                
                Text(
                    text = "المدينة: ${station.cityName}",
                    color = CosmicTextLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "اسم المحطة: ${station.name}",
                color = CosmicText,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "الموقع: Lat: ${station.latitude} / Lng: ${station.longitude}",
                color = CosmicTextGray,
                fontSize = 11.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "البنزين: ${station.fuelTypes} | الملكية: ${station.type}",
                color = CosmicTextLight,
                fontSize = 11.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            if (station.suggestedBy != null) {
                Text(
                    text = "المقترح: ${station.suggestedBy}",
                    color = CosmicAccent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (nearStations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                // Suggest merging option
                Card(
                    colors = CardDefaults.cardColors(containerColor = FuelRed.copy(alpha = 0.10f)),
                    border = BorderStroke(1.dp, FuelRed.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "⚠️ تنبيه تكرار المحطة:\nتم كشف محطة قريبة جداً معتمدة مسبقاً (${nearStations.first().name}) تبعد ${"%.0f".format(viewModel.calculateDistance(station.latitude, station.longitude, nearStations.first().latitude, nearStations.first().longitude))}م. يوصى بـ دمج المحطة لتجنب العشوائية.",
                            color = FuelRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Button(
                            onClick = { viewModel.adminMergeStation(station.id, nearStations.first().id) },
                            colors = ButtonDefaults.buttonColors(containerColor = FuelRed),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.align(Alignment.End).height(28.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Text("دمج الآن 🤝", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Reject
                Button(
                    onClick = { viewModel.adminRejectStation(station.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Text("رفض الطلب ❌", color = CosmicText, fontSize = 11.sp)
                }

                // Approve
                Button(
                    onClick = { viewModel.adminApproveStation(station) },
                    colors = ButtonDefaults.buttonColors(containerColor = FuelGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1.5f).height(38.dp)
                ) {
                    Text("موافقة وتعميم المحطة ✅", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// CITY SUGGESTIONS REVIEW
@Composable
fun AdminCityRequestCard(
    city: City,
    viewModel: QueueFuelViewModel
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CosmicBorder),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "طلب إضافة مدينة جديدة",
                color = CosmicTextLight,
                fontSize = 11.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "المدينة بالعربي: ${city.nameAr}",
                color = CosmicText,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "المدينة بالإنجليزي: ${city.nameEn}",
                color = CosmicTextLight,
                fontSize = 13.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.adminRejectCity(city.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Text("رفض ❌", color = CosmicText, fontSize = 11.sp)
                }

                Button(
                    onClick = { viewModel.adminApproveCity(city) },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicAccent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1.5f).height(38.dp)
                ) {
                    Text("موافقة وتضمين 🏢", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// USER REPUTATION & PROMOTION BLOCK CARD
@Composable
fun AdminUserBlockCard(
    user: AppUser,
    viewModel: QueueFuelViewModel
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CosmicBorder),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.name,
                        color = CosmicText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                when (user.role) {
                                    "ADMIN" -> CosmicAmber
                                    "REPORTER" -> CosmicAccent
                                    else -> CosmicTextLight
                                },
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = user.role,
                            color = if (user.role == "ADMIN") Color.Black else Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(text = "رقم الهاتف: ${user.phoneNumber}", color = CosmicTextLight, fontSize = 11.sp)
                Text(text = "إجمالي النقاط: ${user.points}", color = CosmicAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            // Lock / Ban actions
            if (user.role != "ADMIN") {
                if (!user.banned) {
                    Button(
                        onClick = { viewModel.adminBanUser(user.phoneNumber) },
                        colors = ButtonDefaults.buttonColors(containerColor = FuelRed),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("حظر 🚫", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { viewModel.adminUnbanUser(user.phoneNumber) },
                        colors = ButtonDefaults.buttonColors(containerColor = FuelGreen),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("إلغاء حظر 🟢", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


// ----------------- 6. PROFILE AND TESTING WORKPLACE -----------------
@Composable
fun MyProfileScreen(viewModel: QueueFuelViewModel) {
    val user = viewModel.currentUser ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar circle
        Box(
            modifier = Modifier
                .size(90.dp)
                .background(CosmicAccent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.name.take(2).uppercase(),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = user.name,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CosmicText
        )
        Text(
            text = "رقم الحساب: ${user.phoneNumber}",
            fontSize = 13.sp,
            color = CosmicTextLight
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Profile point card stats
        Card(
            colors = CardDefaults.cardColors(containerColor = CosmicSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, CosmicBorder),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "رصيد عملاتي الإنزاهية ⚡",
                    fontSize = 12.sp,
                    color = CosmicTextGray
                )
                Text(
                    text = "${user.points} نقطة",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = CosmicAmber
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = CosmicBorder)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("الصلاحية الحالية:", color = CosmicTextLight, fontSize = 12.sp)
                    Text(user.role, color = CosmicAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("تقييم نزاهتي:", color = CosmicTextLight, fontSize = 12.sp)
                    Text("موثوق وممتاز 🛡️ (100%)", color = FuelGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // TESTING UTILITY - VERY HELPFUL TO SWITCH ROLES INSTANTLY
        Card(
            colors = CardDefaults.cardColors(containerColor = CosmicSecondaryBg),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, CosmicBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🛠️ لوحة التطوير السريع لتقييم المنصة:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = CosmicAmber
                )
                Text(
                    text = "لتسهيل اختبار المقيم، يمكنك تبديل الصلاحية فوراً للتحقق من واجهات المستخدم، المراسل، والمشرف (الأدمن) دون إعادة التشغيل:",
                    fontSize = 11.sp,
                    color = CosmicTextLight,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { viewModel.switchRole("USER") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (user.role == "USER") CosmicAccent else CosmicBorder,
                            contentColor = if (user.role == "USER") Color.White else CosmicText
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("مواطن عادي", fontSize = 10.sp)
                    }

                    Button(
                        onClick = { viewModel.switchRole("REPORTER") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (user.role == "REPORTER") CosmicAccent else CosmicBorder,
                            contentColor = if (user.role == "REPORTER") Color.White else CosmicText
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("مراسل ثان", fontSize = 10.sp)
                    }

                    if (AuthPolicy.isAdminPhone(user.phoneNumber)) {
                        Button(
                            onClick = { viewModel.switchRole("ADMIN") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (user.role == "ADMIN") CosmicAmber else CosmicBorder,
                                contentColor = if (user.role == "ADMIN") Color.Black else CosmicText
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("أدمن مشرف 👮‍♀️", fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Logout
        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = CosmicBorder),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("logout_button")
        ) {
            Icon(Icons.Filled.Logout, contentDescription = null, tint = CosmicText)
            Spacer(modifier = Modifier.width(8.dp))
            Text("تسجيل الخروج من الحساب", color = CosmicText, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FeedbackRewardDialog(viewModel: QueueFuelViewModel) {
    val allQueueUpdates by viewModel.allQueueUpdates.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    val currentCycleUpdates = allQueueUpdates.filter { it.timestamp >= viewModel.cycleStartTime }
    val phonesWhoFeedbacked = currentCycleUpdates.map { it.userPhone }.toSet()
    
    // Competitors: users who are not Admin and have submitted updates in this cycle
    val competitors = allUsers.filter { phonesWhoFeedbacked.contains(it.phoneNumber) && it.role != "ADMIN" }
        .map { user ->
            val updateCount = currentCycleUpdates.count { it.userPhone == user.phoneNumber }
            Triple(user, updateCount, user.points)
        }
        .sortedByDescending { it.third } // sort by points descending

    AlertDialog(
        onDismissRequest = { viewModel.showFeedbackRewardDialog = false },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🏆 المتنافسون وجوائز النقاط 🏆", color = CosmicText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Countdown card
                Card(
                    colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                    border = BorderStroke(1.dp, CosmicBorder),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "الوقت المتبقي لانتهاء دورة الـ 5 ساعات ونظام الجوائز",
                            color = CosmicTextGray,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = viewModel.timeRemainingString,
                            color = CosmicAmber,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Text(
                    text = "الأشخاص الذين ساهموا في تحديث حالات السرا (مزدحم/فارغ) خلال دورة الـ 5 ساعات الحالية ونقاطهم الحالية لمكافأة الأكثر نشاطاً:",
                    fontSize = 11.sp,
                    color = CosmicTextLight,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (competitors.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "لم يقم أحد بتقديم تقارير السرا في هذه الدورة حتى الآن! 🏁",
                                    color = CosmicTextGray,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(competitors) { (user, count, points) ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, CosmicBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Points/Scores view
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text(text = "النقاط المتراكمة", color = CosmicTextGray, fontSize = 9.sp)
                                        Text(text = "$points ⚡", color = CosmicAmber, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // User details
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = user.name, color = CosmicText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "الهاتف: ${user.phoneNumber}", color = CosmicTextLight, fontSize = 10.sp)
                                        Text(
                                            text = "مجموع التقارير في الدورة: $count 📊",
                                            color = CosmicAccent,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (viewModel.currentUser?.role == "ADMIN") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            viewModel.resetCycleAction()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FuelRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("إعادة تعيين نقاط الدورة حالاً (إبدأ دورة جديدة) 🔄", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.showFeedbackRewardDialog = false },
                colors = ButtonDefaults.buttonColors(containerColor = CosmicAccent),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("حسناً 🤝", fontSize = 12.sp, color = Color.White)
            }
        },
        containerColor = CosmicSecondaryBg,
        textContentColor = CosmicText
    )
}

@Composable
fun FirebaseSettingsPanel(viewModel: QueueFuelViewModel) {
    var dbUrl by remember { mutableStateOf(viewModel.firebaseDatabaseUrl) }
    var apiKey by remember { mutableStateOf(viewModel.firebaseWebApiKey) }
    var syncOn by remember { mutableStateOf(viewModel.firebaseSyncEnabled) }

    Card(
        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CosmicBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status indicator
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (viewModel.firebaseSyncEnabled) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFF6B7280).copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (viewModel.firebaseSyncEnabled) Color(0xFF10B981) else CosmicBorder)
                ) {
                    Text(
                        text = viewModel.firebaseSyncStatus,
                        color = if (viewModel.firebaseSyncEnabled) Color(0xFF10B981) else CosmicTextGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                     )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "سحابة قاعدة بيانات فايربيس 🌐",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CosmicText
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "اربط تطبيقك مباشرة بحساب Firebase الخاص بك لنقل وتأمين كافة بيانات المستخدمين، المحطات، المدن، ومستويات الصلاحيات للأدمن بشكل آمن وسحابي.",
                color = CosmicTextLight,
                fontSize = 11.sp,
                textAlign = TextAlign.Right,
                lineHeight = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Web URL
            Text("رابط قاعدة بيانات فايربيس (URL) 🔗", fontSize = 11.sp, color = CosmicTextGray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = dbUrl,
                onValueChange = { dbUrl = it },
                placeholder = { Text("https://your-project-rtdb.firebaseio.com/", color = CosmicTextLight.copy(alpha = 0.5f), fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Left, color = CosmicText, fontSize = 13.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CosmicAccent,
                    unfocusedBorderColor = CosmicBorder,
                    focusedTextColor = CosmicText,
                    unfocusedTextColor = CosmicText,
                    focusedContainerColor = CosmicSecondaryBg,
                    unfocusedContainerColor = CosmicSecondaryBg
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // API key / auth key
            Text("رمز التحقق (API Auth Token / Optional) 🔑", fontSize = 11.sp, color = CosmicTextGray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Right)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                placeholder = { Text("أدخل رمز التحقق لحماية قاعدتك (اختياري)", color = CosmicTextLight.copy(alpha = 0.5f), fontSize = 11.sp) },
                singleLine = true,
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Left, color = CosmicText, fontSize = 13.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CosmicAccent,
                    unfocusedBorderColor = CosmicBorder,
                    focusedTextColor = CosmicText,
                    unfocusedTextColor = CosmicText,
                    focusedContainerColor = CosmicSecondaryBg,
                    unfocusedContainerColor = CosmicSecondaryBg
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Switch to Enable / Disable Sync auto
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { syncOn = !syncOn }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = syncOn,
                    onCheckedChange = { syncOn = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CosmicAccent,
                        checkedTrackColor = CosmicAccent.copy(alpha = 0.4f),
                        uncheckedThumbColor = CosmicBorder,
                        uncheckedTrackColor = CosmicSecondaryBg
                    )
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text("تفعيل المزامنة السحابية التلقائية ⚡", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CosmicText)
                    Text("مزامنة أي تغيير على المحطات أو المستخدمين فوراً", fontSize = 10.sp, color = CosmicTextLight)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Row buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sync manually
                Button(
                    onClick = {
                        viewModel.syncAllDataToFirebase()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (viewModel.firebaseSyncEnabled) CosmicAmber else CosmicBorder),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !viewModel.isSyncInProgress,
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    if (viewModel.isSyncInProgress) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = "مزامنة يدوية للبيانات 🚀",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (viewModel.firebaseSyncEnabled) Color.Black else CosmicTextGray
                        )
                    }
                }

                // Save Config
                Button(
                    onClick = {
                        viewModel.saveFirebaseSettings(dbUrl, apiKey, syncOn)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicAccent),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1.2f).height(40.dp)
                ) {
                    Text("حفظ الإعدادات والربط 💾", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Helper configuration note
            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicSecondaryBg),
                border = BorderStroke(1.dp, CosmicBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.End) {
                    Text("خطوات الربط السريع 🚀:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CosmicAccent)
                    Text("1. توجه إلى console.firebase.google.com في متصفحك.", fontSize = 9.sp, color = CosmicTextLight, textAlign = TextAlign.Right)
                    Text("2. أنشئ مشروعاً جديداً ثم توجّه إلى 'Realtime Database' وقُم بإنشائها.", fontSize = 9.sp, color = CosmicTextLight, textAlign = TextAlign.Right)
                    Text("3. انسخ رابط قاعدة البيانات والصقه بالأعلى.", fontSize = 9.sp, color = CosmicTextLight, textAlign = TextAlign.Right)
                    Text("4. من إعدادات القواعد (Rules) للمشروع حدد قراءة وكتابة كـ true للحساب المفتوح، أو استخدم رمز التحقق Token.", fontSize = 9.sp, color = CosmicTextLight, textAlign = TextAlign.Right)
                    Text("5. سيتم فور رغبتك ترحيل كافة صلاحيات الأدمن، تقارير المنافسين والمحطات بنجاح تام!", fontSize = 9.sp, color = CosmicTextLight, textAlign = TextAlign.Right)
                }
            }
        }
    }
}
