package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.domain.model.*
import com.example.domain.usecase.AuthPolicy
import com.example.domain.usecase.GeoProximity
import com.example.domain.usecase.ReportVerification
import com.example.ui.components.QfSubScreenTopBar
import com.example.ui.components.QueueFuelLogo
import com.example.ui.components.QueueFuelSplashScreen
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MapScreen
import com.example.ui.screens.PlaceDetailsScreen
import com.example.ui.screens.ReportWizardScreen
import com.example.ui.screens.RewardsScreen
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

// Bottom navigation of the design board: الرئيسية / الخريطة / تقرير + / المكافآت / الحساب
enum class NavigationTab {
    HOME,       // الرئيسية
    MAP,        // الخريطة
    REWARDS,    // المكافآت
    ACCOUNT     // الحساب
}

// Secondary surfaces pushed on top of a tab (with a back top-bar)
enum class SubScreen {
    NONE,
    ALERTS,        // التنبيهات وسجل التحديثات
    SUGGESTIONS,   // اقتراح محطة/مدينة
    ADMIN          // لوحة الأدمن
}

@Composable
fun QueueFuelApp(viewModel: QueueFuelViewModel = viewModel()) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(NavigationTab.HOME) }
    var subScreen by remember { mutableStateOf(SubScreen.NONE) }
    var showReportWizard by remember { mutableStateOf(false) }
    var reportInitialStation by remember { mutableStateOf<Station?>(null) }
    var showSplash by remember { mutableStateOf(true) }

    // Collect toast events from ViewModel and show them in the UI layer
    LaunchedEffect(Unit) {
        viewModel.toastEvent.collectLatest { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    if (showSplash) {
        QueueFuelSplashScreen(onFinished = { showSplash = false })
        return
    }

    // If not logged in, show the registration screen
    if (!viewModel.isLoggedIn) {
        LoginScreen(viewModel = viewModel)
        return
    }

    val detailStation = viewModel.selectedStation

    when {
        // Report wizard — highest overlay (reachable from details or the + tab)
        showReportWizard -> {
            BackHandler {
                showReportWizard = false
                reportInitialStation = null
            }
            ReportWizardScreen(
                viewModel = viewModel,
                initialStation = reportInitialStation,
                onClose = {
                    showReportWizard = false
                    reportInitialStation = null
                }
            )
        }

        // Place details — opened from home cards or the map bottom card
        detailStation != null -> {
            BackHandler { viewModel.selectedStation = null }
            PlaceDetailsScreen(
                station = detailStation,
                viewModel = viewModel,
                onBack = { viewModel.selectedStation = null },
                onNewReport = { station ->
                    reportInitialStation = station
                    showReportWizard = true
                }
            )
        }

        // Pushed secondary surfaces (alerts / suggestions / admin)
        subScreen != SubScreen.NONE -> {
            BackHandler { subScreen = SubScreen.NONE }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(QfNavy)
                    .navigationBarsPadding()
            ) {
                QfSubScreenTopBar(
                    title = when (subScreen) {
                        SubScreen.ALERTS -> "التنبيهات"
                        SubScreen.SUGGESTIONS -> "اقتراح جديد"
                        SubScreen.ADMIN -> "لوحة الإشراف"
                        SubScreen.NONE -> ""
                    },
                    onBack = { subScreen = SubScreen.NONE }
                )
                when (subScreen) {
                    SubScreen.ALERTS -> AlertsAndSecurityLog(viewModel)
                    SubScreen.SUGGESTIONS -> SuggestionsAndClaims(viewModel)
                    SubScreen.ADMIN -> AdminDashboardPanel(viewModel)
                    SubScreen.NONE -> {}
                }
            }
        }

        // Main tabs
        else -> {
            Scaffold(
                bottomBar = {
                    QueueFuelBottomBar(
                        currentTab = currentTab,
                        onTabSelected = { currentTab = it },
                        onReportClick = {
                            reportInitialStation = null
                            showReportWizard = true
                        }
                    )
                },
                containerColor = QfNavy
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(QfNavy)
                ) {
                    Crossfade(targetState = currentTab, label = "TabTransition") { tab ->
                        when (tab) {
                            NavigationTab.HOME -> HomeScreen(
                                viewModel = viewModel,
                                onOpenAlerts = { subScreen = SubScreen.ALERTS },
                                onStationClick = { viewModel.selectedStation = it }
                            )
                            NavigationTab.MAP -> MapScreen(
                                viewModel = viewModel,
                                onOpenDetails = { viewModel.selectedStation = it }
                            )
                            NavigationTab.REWARDS -> RewardsScreen(viewModel)
                            NavigationTab.ACCOUNT -> AccountScreen(
                                viewModel = viewModel,
                                onOpenAlerts = { subScreen = SubScreen.ALERTS },
                                onOpenSuggestions = { subScreen = SubScreen.SUGGESTIONS },
                                onOpenAdmin = { subScreen = SubScreen.ADMIN }
                            )
                        }
                    }
                }
            }
        }
    }

    if (viewModel.showFeedbackRewardDialog) {
        FeedbackRewardDialog(viewModel = viewModel)
    }
}

/**
 * Bottom bar matching the mockup: four tabs around a raised teal "تقرير +"
 * action button in the center.
 */
@Composable
fun QueueFuelBottomBar(
    currentTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    onReportClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Transparent strip that lets the center button protrude above the bar
            Spacer(modifier = Modifier.height(26.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(QfSurface)
                    .navigationBarsPadding()
                    .height(62.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomBarItem(
                    label = "الرئيسية",
                    icon = Icons.Filled.Home,
                    selected = currentTab == NavigationTab.HOME,
                    onClick = { onTabSelected(NavigationTab.HOME) },
                    modifier = Modifier.weight(1f).testTag("nav_home")
                )
                BottomBarItem(
                    label = "الخريطة",
                    icon = Icons.Filled.Map,
                    selected = currentTab == NavigationTab.MAP,
                    onClick = { onTabSelected(NavigationTab.MAP) },
                    modifier = Modifier.weight(1f).testTag("nav_map")
                )
                // Center slot: label under the floating action button
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = "تقرير +",
                        color = QfTurquoise,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                BottomBarItem(
                    label = "المكافآت",
                    icon = Icons.Filled.EmojiEvents,
                    selected = currentTab == NavigationTab.REWARDS,
                    onClick = { onTabSelected(NavigationTab.REWARDS) },
                    modifier = Modifier.weight(1f).testTag("nav_rewards")
                )
                BottomBarItem(
                    label = "الحساب",
                    icon = Icons.Filled.Person,
                    selected = currentTab == NavigationTab.ACCOUNT,
                    onClick = { onTabSelected(NavigationTab.ACCOUNT) },
                    modifier = Modifier.weight(1f).testTag("nav_account")
                )
            }
        }

        // Raised primary report action
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(54.dp)
                .border(3.dp, QfNavy, CircleShape)
                .clip(CircleShape)
                .background(QfDeepTeal)
                .clickable(onClick = onReportClick)
                .testTag("nav_report"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "تقرير جديد",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun BottomBarItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) QfTurquoise else QfTextTertiary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            color = if (selected) QfTurquoise else QfTextTertiary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
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
            // Master brand mark: location pin + people queue
            QueueFuelLogo(size = 84.dp)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "QueueFuel",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CosmicText,
                textAlign = TextAlign.Center
            )

            Text(
                text = "كل الطوابير... بمكان واحد",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = QfTurquoise,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = "معلومات الازدحام لحظة بلحظة — نبدأ بمحطات الوقود في كركوك، أربيل، والسليمانية",
                fontSize = 12.sp,
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


// Status Prop helper — the four queue states of the design system:
// empty = green, medium = orange, crowded = red, closed = gray.
data class StatusProps(val label: String, val color: Color)
fun getQueueStatusProps(status: String): StatusProps {
    return when (status) {
        "EMPTY" -> StatusProps("فارغة", QfSuccess)
        "SHORT" -> StatusProps("قصيرة", QfSuccessLight)
        "MODERATE" -> StatusProps("متوسطة", QfWarning)
        "LONG" -> StatusProps("مزدحمة", QfError)
        else -> StatusProps("مغلقة", QfClosed)
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
            tint = QfTextTertiary
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text("لم نجد محطات تطابق معايير الفرز هذه في المدينة المحددة.", color = QfTextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
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
    return stations.any { st ->
        GeoProximity.distanceMeters(lat, lng, st.latitude, st.longitude) < 100.0
    }
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
                text = adminTabLabel("المحطات المعلقة (${pendingStations.size})")
            )
            Tab(
                selected = adminSectionTab == 1,
                onClick = { adminSectionTab = 1 },
                text = adminTabLabel("المدن المعلقة (${pendingCities.size})")
            )
            Tab(
                selected = adminSectionTab == 2,
                onClick = { adminSectionTab = 2 },
                text = adminTabLabel("إدارة الحسابات")
            )
            Tab(
                selected = adminSectionTab == 3,
                onClick = { adminSectionTab = 3 },
                text = adminTabLabel("سحاب الفايربيس 🌐")
            )
            Tab(
                selected = adminSectionTab == 4,
                onClick = { adminSectionTab = 4 },
                text = adminTabLabel("التقارير 📋 ($unverifiedReports)"),
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
fun adminTabLabel(txt: String): @Composable () -> Unit = {
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
                        containerColor = if (viewModel.firebaseSyncEnabled) QfSuccess.copy(alpha = 0.15f) else QfClosed.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (viewModel.firebaseSyncEnabled) QfSuccess else CosmicBorder)
                ) {
                    Text(
                        text = viewModel.firebaseSyncStatus,
                        color = if (viewModel.firebaseSyncEnabled) QfSuccess else CosmicTextGray,
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
