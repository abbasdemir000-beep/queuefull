package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AdBanner
import com.example.domain.model.QueueCategory
import com.example.domain.model.Station
import com.example.ui.EmptyStationsState
import com.example.ui.QueueFuelViewModel
import com.example.ui.components.QfCategoriesGrid
import com.example.ui.components.QfChip
import com.example.ui.components.QfSearchBar
import com.example.ui.components.QfSectionHeader
import com.example.ui.components.QfStatusBadge
import com.example.ui.formatTime
import com.example.ui.getQueueStatusProps
import com.example.ui.theme.*

/**
 * Home — the universal-queues entry point of the design board:
 * search bar on top, category grid first, then the nearest queues.
 * Filtering is reduced to one row of compact chips (no filter panel).
 */
@Composable
fun HomeScreen(
    viewModel: QueueFuelViewModel,
    onOpenAlerts: () -> Unit,
    onStationClick: (Station) -> Unit
) {
    val context = LocalContext.current
    val approvedCities by viewModel.approvedCities.collectAsState()
    val allBanners by viewModel.allBanners.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    // Subscribing here starts the WhileSubscribed station stream and recomposes
    // this screen on Room emissions; filteredStationsList reads the same value.
    val approvedStations by viewModel.approvedStations.collectAsState()
    val filteredStations = if (approvedStations.isEmpty()) emptyList() else viewModel.filteredStationsList

    val activeCity = approvedCities.find { it.id == viewModel.selectedCityId }
    val cityBanner: AdBanner? = activeCity?.let { city ->
        allBanners.firstOrNull { it.city == city.nameAr }
    }

    val nearestStations = filteredStations.sortedBy { st ->
        viewModel.calculateDistance(
            viewModel.currentLatitude, viewModel.currentLongitude,
            st.latitude, st.longitude
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Search bar + notifications bell
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QfSearchBar(
                    value = viewModel.searchQuery,
                    onValueChange = { viewModel.searchQuery = it },
                    placeholder = "ابحث عن طابور أو مكان...",
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_search")
                )
                IconButton(
                    onClick = onOpenAlerts,
                    modifier = Modifier
                        .size(48.dp)
                        .background(QfSurfaceVariant, RoundedCornerShape(14.dp))
                        .testTag("home_alerts_button")
                ) {
                    BadgedBox(badge = {
                        if (notifications.isNotEmpty()) {
                            Badge { Text(notifications.size.toString()) }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "التنبيهات",
                            tint = QfTextPrimary
                        )
                    }
                }
            }
        }

        // Universal queue categories — always first, fuel is the live one
        item {
            QfSectionHeader(title = "كل الطوابير... بمكان واحد")
            QfCategoriesGrid(
                selectedCategory = QueueCategory.FUEL,
                onCategoryClick = { category ->
                    if (category.comingSoon) {
                        Toast.makeText(
                            context,
                            "${category.labelAr} — قريباً في QueueFuel",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }

        // City selector + compact filter chips (one scrollable row each)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                approvedCities.forEach { city ->
                    QfChip(
                        label = city.nameAr,
                        selected = viewModel.selectedCityId == city.id,
                        onClick = { viewModel.selectedCityId = city.id },
                        modifier = Modifier.testTag("home_city_chip_${city.id}")
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QfChip(
                    label = "فارغة",
                    selected = viewModel.filterStatusEmpty,
                    onClick = { viewModel.filterStatusEmpty = !viewModel.filterStatusEmpty },
                    selectedColor = QfSuccess
                )
                QfChip(
                    label = "قصيرة",
                    selected = viewModel.filterStatusShort,
                    onClick = { viewModel.filterStatusShort = !viewModel.filterStatusShort },
                    selectedColor = QfSuccessLight
                )
                QfChip(
                    label = "حكومية",
                    selected = viewModel.filterGovOnly,
                    onClick = {
                        viewModel.filterGovOnly = !viewModel.filterGovOnly
                        if (viewModel.filterGovOnly) viewModel.filterPrivateOnly = false
                    }
                )
                QfChip(
                    label = "أهلية",
                    selected = viewModel.filterPrivateOnly,
                    onClick = {
                        viewModel.filterPrivateOnly = !viewModel.filterPrivateOnly
                        if (viewModel.filterPrivateOnly) viewModel.filterGovOnly = false
                    }
                )
                QfChip(
                    label = "بنزين عادي",
                    selected = viewModel.filterFuelNormal,
                    onClick = { viewModel.filterFuelNormal = !viewModel.filterFuelNormal }
                )
                QfChip(
                    label = "محسن",
                    selected = viewModel.filterFuelPremium,
                    onClick = { viewModel.filterFuelPremium = !viewModel.filterFuelPremium }
                )
                QfChip(
                    label = "سوبر",
                    selected = viewModel.filterFuelSuper,
                    onClick = { viewModel.filterFuelSuper = !viewModel.filterFuelSuper }
                )
            }
        }

        // Sponsored banner for the active city
        cityBanner?.let { ad ->
            item { HomeBannerAdCard(ad = ad) }
        }

        // Nearest queues
        item {
            QfSectionHeader(
                title = "أقرب الطوابير${activeCity?.let { " — ${it.nameAr}" } ?: ""}",
                trailing = if (nearestStations.isEmpty()) "لا نتائج" else "${nearestStations.size} مكان"
            )
        }

        if (nearestStations.isEmpty()) {
            item { EmptyStationsState() }
        } else {
            items(nearestStations) { station ->
                QueueItemCard(
                    station = station,
                    distanceMeters = viewModel.calculateDistance(
                        viewModel.currentLatitude, viewModel.currentLongitude,
                        station.latitude, station.longitude
                    ),
                    onClick = { onStationClick(station) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

/** Compact queue card: category icon, name, status badge, distance, recency. */
@Composable
fun QueueItemCard(
    station: Station,
    distanceMeters: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusProps = getQueueStatusProps(station.queueStatus)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable(onClick = onClick)
            .testTag("station_card_${station.id}"),
        colors = CardDefaults.cardColors(containerColor = QfSurface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, QfBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(QfTealContainer, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalGasStation,
                    contentDescription = "وقود",
                    tint = QfOnTealContainer,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = station.name,
                    color = QfTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.NearMe,
                        contentDescription = null,
                        tint = QfTextTertiary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${"%.1f".format(distanceMeters / 1000.0)} كم",
                        color = QfTextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .background(QfTextTertiary, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatTime(station.lastUpdated),
                        color = QfTextTertiary,
                        fontSize = 11.sp
                    )
                }
            }

            QfStatusBadge(label = statusProps.label, color = statusProps.color)
        }
    }
}

/** Sponsored-ad banner shown for the active city (kept from the previous UI). */
@Composable
fun HomeBannerAdCard(ad: AdBanner) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("ad_banner_card"),
        colors = CardDefaults.cardColors(containerColor = QfSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, QfDeepTeal.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(QfGold, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("إعلان ممول", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = ad.category,
                    color = QfTextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = ad.title,
                color = QfTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = ad.description,
                color = QfTextSecondary,
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
                    colors = ButtonDefaults.buttonColors(containerColor = QfDeepTeal),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.PhoneInTalk, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("اتصل الآن", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
