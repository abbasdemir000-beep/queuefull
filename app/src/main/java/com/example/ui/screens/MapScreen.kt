package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Station
import com.example.ui.QueueFuelViewModel
import com.example.ui.components.QfChip
import com.example.ui.components.QfStatusBadge
import com.example.ui.getQueueStatusProps
import com.example.ui.theme.*
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Map — full-screen dark map canvas with colored queue pins
 * (green = empty, light green = short, orange = medium, red = crowded,
 * gray = closed/unknown). Tapping a pin raises a bottom card whose CTA
 * "عرض التفاصيل" opens the place-details screen.
 */
@Composable
fun MapScreen(
    viewModel: QueueFuelViewModel,
    onOpenDetails: (Station) -> Unit
) {
    val approvedCities by viewModel.approvedCities.collectAsState()
    // Subscribing here starts the WhileSubscribed station stream and recomposes
    // this screen on Room emissions; filteredStationsList reads the same value.
    val approvedStations by viewModel.approvedStations.collectAsState()
    val stations = if (approvedStations.isEmpty()) emptyList() else viewModel.filteredStationsList
    var pinnedStation by remember { mutableStateOf<Station?>(null) }

    // Drop the pinned card if the station left the filtered set
    LaunchedEffect(stations) {
        if (pinnedStation != null && stations.none { it.id == pinnedStation?.id }) {
            pinnedStation = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        QueueMapCanvas(
            viewModel = viewModel,
            stations = stations,
            onStationTap = { pinnedStation = it },
            modifier = Modifier
                .fillMaxSize()
                .testTag("map_canvas")
        )

        // City chips overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 10.dp)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            approvedCities.forEach { city ->
                QfChip(
                    label = city.nameAr,
                    selected = viewModel.selectedCityId == city.id,
                    onClick = {
                        viewModel.selectedCityId = city.id
                        pinnedStation = null
                    }
                )
            }
        }

        // Status legend overlay
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 56.dp)
                .background(QfSurface.copy(alpha = 0.92f), RoundedCornerShape(10.dp))
                .border(BorderStroke(0.5.dp, QfBorder), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MapLegendDot("فارغ", QfSuccess)
            MapLegendDot("متوسط", QfWarning)
            MapLegendDot("مزدحم", QfError)
            MapLegendDot("مغلق", QfClosed)
        }

        // Location controls: real GPS (FusedLocationProvider) plus the MVP's
        // simulation toggle as fallback. A fresh real fix wins everywhere.
        val locationPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            viewModel.onLocationPermissionResult(granted)
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = if (pinnedStation != null) 168.dp else 16.dp)
                .background(QfSurface.copy(alpha = 0.92f), RoundedCornerShape(12.dp))
                .border(BorderStroke(0.5.dp, QfBorder), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(
                onClick = {
                    if (viewModel.locationPermissionGranted) {
                        viewModel.refreshRealLocation()
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                modifier = Modifier
                    .size(34.dp)
                    .testTag("map_locate_me_button")
            ) {
                if (viewModel.isLocating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = QfTurquoise,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.MyLocation,
                        contentDescription = "تحديد موقعي الحقيقي",
                        tint = if (viewModel.usingRealLocation) QfTurquoise else QfTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = if (viewModel.usingRealLocation) "GPS حقيقي" else "GPS",
                color = if (viewModel.usingRealLocation) QfTurquoise else QfTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Switch(
                checked = viewModel.simulateGpsEnabled,
                onCheckedChange = { viewModel.simulateGpsEnabled = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = QfDeepTeal
                ),
                modifier = Modifier.scale(0.8f)
            )
        }

        // Bottom card for the selected place
        pinnedStation?.let { station ->
            val statusProps = getQueueStatusProps(station.queueStatus)
            val distance = viewModel.calculateDistance(
                viewModel.currentLatitude, viewModel.currentLongitude,
                station.latitude, station.longitude
            )
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(12.dp)
                    .testTag("map_bottom_card"),
                colors = CardDefaults.cardColors(containerColor = QfSurface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                border = BorderStroke(1.dp, QfBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(QfTealContainer, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalGasStation,
                                contentDescription = "وقود",
                                tint = QfOnTealContainer,
                                modifier = Modifier.size(22.dp)
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
                            Text(
                                text = "${station.cityName} • يبعد ${"%.1f".format(distance / 1000.0)} كم",
                                color = QfTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        QfStatusBadge(label = statusProps.label, color = statusProps.color)
                        IconButton(onClick = { pinnedStation = null }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = "إغلاق", tint = QfTextTertiary, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onOpenDetails(station) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("map_view_details_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = QfDeepTeal),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("عرض التفاصيل", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun MapLegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .background(color, CircleShape)
        )
        Text(label, color = QfTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

/**
 * Simulated dark map: navy canvas, grid blocks, road network and queue pins
 * projected from real station coordinates around the city center.
 */
@Composable
fun QueueMapCanvas(
    viewModel: QueueFuelViewModel,
    stations: List<Station>,
    onStationTap: (Station) -> Unit,
    modifier: Modifier = Modifier
) {
    val userLat = viewModel.currentLatitude
    val userLng = viewModel.currentLongitude

    val centerLat = if (stations.isNotEmpty()) stations.map { it.latitude }.average() else userLat
    val centerLng = if (stations.isNotEmpty()) stations.map { it.longitude }.average() else userLng

    val latMapRange = 0.08
    val lngMapRange = 0.08

    Canvas(
        modifier = modifier
            .background(QfNavy)
            .pointerInput(stations) {
                detectTapGestures { offset ->
                    val width = size.width
                    val height = size.height

                    var closestStation: Station? = null
                    var minDistance = Float.MAX_VALUE

                    stations.forEach { st ->
                        val relLng = (st.longitude - centerLng) / lngMapRange
                        val relLat = (st.latitude - centerLat) / latMapRange
                        val x = (width / 2f) + (relLng * width).toFloat()
                        val y = (height / 2f) - (relLat * height).toFloat()
                        val tapDist = sqrt((offset.x - x).pow(2) + (offset.y - y).pow(2))
                        if (tapDist < 60f && tapDist < minDistance) {
                            closestStation = st
                            minDistance = tapDist
                        }
                    }

                    closestStation?.let(onStationTap)
                }
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // City-block grid backdrop
        val gridSpacing = 56f
        for (x in 0..(canvasWidth / gridSpacing).toInt()) {
            drawLine(
                color = QfBorder.copy(alpha = 0.35f),
                start = Offset(x * gridSpacing, 0f),
                end = Offset(x * gridSpacing, canvasHeight),
                strokeWidth = 1f
            )
        }
        for (y in 0..(canvasHeight / gridSpacing).toInt()) {
            drawLine(
                color = QfBorder.copy(alpha = 0.35f),
                start = Offset(0f, y * gridSpacing),
                end = Offset(canvasWidth, y * gridSpacing),
                strokeWidth = 1f
            )
        }

        // Main roads
        val roadColor = QfSurfaceVariant
        drawLine(roadColor, Offset(0f, canvasHeight * 0.30f), Offset(canvasWidth, canvasHeight * 0.30f), 28f)
        drawLine(roadColor, Offset(0f, canvasHeight * 0.62f), Offset(canvasWidth, canvasHeight * 0.62f), 20f)
        drawLine(roadColor, Offset(canvasWidth * 0.28f, 0f), Offset(canvasWidth * 0.28f, canvasHeight), 24f)
        drawLine(roadColor, Offset(canvasWidth * 0.72f, 0f), Offset(canvasWidth * 0.72f, canvasHeight), 18f)
        drawLine(roadColor, Offset(0f, canvasHeight * 0.86f), Offset(canvasWidth, canvasHeight * 0.86f), 14f)

        // Queue pins
        stations.forEach { st ->
            val relLng = (st.longitude - centerLng) / lngMapRange
            val relLat = (st.latitude - centerLat) / latMapRange
            val x = (canvasWidth / 2f) + (relLng * canvasWidth).toFloat()
            val y = (canvasHeight / 2f) - (relLat * canvasHeight).toFloat()

            val statusColor = when (st.queueStatus) {
                "EMPTY" -> QfSuccess
                "SHORT" -> QfSuccessLight
                "MODERATE" -> QfWarning
                "LONG" -> QfError
                else -> QfClosed
            }

            drawCircle(statusColor.copy(alpha = 0.22f), radius = 34f, center = Offset(x, y))
            drawCircle(statusColor, radius = 13f, center = Offset(x, y))
            drawCircle(Color.White, radius = 4.5f, center = Offset(x, y))
        }

        // User location (pulsing teal ring) — drawn for a real fix or while simulating
        if (viewModel.usingRealLocation || viewModel.simulateGpsEnabled) {
            val relUserLng = (userLng - centerLng) / lngMapRange
            val relUserLat = (userLat - centerLat) / latMapRange
            val cursorX = (canvasWidth / 2f) + (relUserLng * canvasWidth).toFloat()
            val cursorY = (canvasHeight / 2f) - (relUserLat * canvasHeight).toFloat()

            drawCircle(
                color = QfTurquoise.copy(alpha = 0.35f),
                radius = 44f,
                center = Offset(cursorX, cursorY),
                style = Stroke(width = 3f)
            )
            drawCircle(QfSurface, radius = 9f, center = Offset(cursorX, cursorY))
            drawCircle(QfTurquoise, radius = 5.5f, center = Offset(cursorX, cursorY))
        }
    }
}
