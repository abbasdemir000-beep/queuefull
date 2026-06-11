package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Station
import com.example.ui.QueueFuelViewModel
import com.example.ui.components.QfCard
import com.example.ui.components.QfStatusBadge
import com.example.ui.components.QfSubScreenTopBar
import com.example.ui.formatTime
import com.example.ui.getQueueStatusProps
import com.example.ui.theme.*

/** Rough crowd size estimate per status — display-only UI heuristic. */
fun estimatedQueueLabel(status: String): String = when (status) {
    "EMPTY" -> "0–5 سيارات تقريباً"
    "SHORT" -> "5–15 سيارة تقريباً"
    "MODERATE" -> "15–40 سيارة تقريباً"
    "LONG" -> "أكثر من 40 سيارة"
    else -> "غير معروف"
}

/**
 * Place details — name, category, open/closed, queue badge, last update,
 * reliability, crowd estimate, and the three actions of the design board:
 * تقرير جديد / الاتجاهات / مشاركة. Quick on-site confirmation is kept.
 */
@Composable
fun PlaceDetailsScreen(
    station: Station,
    viewModel: QueueFuelViewModel,
    onBack: () -> Unit,
    onNewReport: (Station) -> Unit
) {
    val context = LocalContext.current
    val statusProps = getQueueStatusProps(station.queueStatus)
    val isOpen = station.queueStatus != "CLOSED"
    val distance = viewModel.calculateDistance(
        viewModel.currentLatitude, viewModel.currentLongitude,
        station.latitude, station.longitude
    )
    val userIsClose = viewModel.isUserNear(station)
    // Display reliability derived from on-site confirmations (capped at 100%)
    val reliabilityPercent = minOf(100, 40 + station.confirmedCount * 6)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QfNavy)
            .navigationBarsPadding()
            .testTag("place_details_screen")
    ) {
        QfSubScreenTopBar(title = "تفاصيل المكان", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Identity card: category icon + name + open state
            QfCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(QfTealContainer, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalGasStation,
                            contentDescription = "وقود",
                            tint = QfOnTealContainer,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = station.name,
                            color = QfTextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "${station.cityName} • ${station.type}",
                            color = QfTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    QfStatusBadge(
                        label = if (isOpen) "مفتوح" else "مغلق",
                        color = if (isOpen) QfSuccess else QfClosed
                    )
                }
            }

            // Live queue state card
            QfCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("حالة الطابور الآن", color = QfTextSecondary, fontSize = 13.sp)
                        QfStatusBadge(label = statusProps.label, color = statusProps.color)
                    }
                    DetailInfoRow(label = "آخر تحديث", value = formatTime(station.lastUpdated))
                    DetailInfoRow(label = "الحجم التقديري", value = estimatedQueueLabel(station.queueStatus))
                    DetailInfoRow(label = "الوقود المتوفر", value = station.fuelTypes)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("الموثوقية", color = QfTextSecondary, fontSize = 13.sp)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ReliabilityStars(confirmedCount = station.confirmedCount)
                            Text(
                                text = "$reliabilityPercent% (${station.confirmedCount} تأكيد)",
                                color = QfGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Proximity hint — drives whether reporting/confirmation is allowed
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        (if (userIsClose) QfSuccess else QfWarning).copy(alpha = 0.10f),
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.dp,
                        (if (userIsClose) QfSuccess else QfWarning).copy(alpha = 0.35f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = if (userIsClose)
                        "أنت ضمن نطاق المكان (${"%.0f".format(distance)}م) — يمكنك التقرير وتأكيد الحالة."
                    else
                        "تبعد ${"%.0f".format(distance)}م عن المكان. التقارير تتطلب التواجد ضمن 200م.",
                    color = if (userIsClose) QfSuccess else QfWarning,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Primary action — opens the report wizard
            Button(
                onClick = { onNewReport(station) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("details_new_report_button"),
                colors = ButtonDefaults.buttonColors(containerColor = QfDeepTeal),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("تقرير جديد", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            // Secondary actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val uri = Uri.parse(
                            "geo:${station.latitude},${station.longitude}?q=${station.latitude},${station.longitude}(${Uri.encode(station.name)})"
                        )
                        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("details_directions_button"),
                    border = BorderStroke(1.dp, QfDeepTeal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Directions, contentDescription = null, tint = QfTurquoise, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("الاتجاهات", color = QfTurquoise, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = {
                        val share = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "${station.name} — حالة الطابور: ${statusProps.label}\n" +
                                    "https://maps.google.com/?q=${station.latitude},${station.longitude}\n" +
                                    "عبر تطبيق QueueFuel — كل الطوابير... بمكان واحد"
                            )
                        }
                        runCatching { context.startActivity(Intent.createChooser(share, "مشاركة")) }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("details_share_button"),
                    border = BorderStroke(1.dp, QfBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null, tint = QfTextSecondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مشاركة", color = QfTextSecondary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // Quick on-site confirmation (+5 pts) — existing feature, kept
            OutlinedButton(
                onClick = { viewModel.confirmStatus(station.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("confirm_station_status"),
                border = BorderStroke(1.dp, QfBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.ThumbUp, contentDescription = null, tint = QfGold, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("أنا هنا وأؤكد الحالة (+5 نقاط)", color = QfTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DetailInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = QfTextSecondary, fontSize = 13.sp)
        Text(value, color = QfTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

/** Three-star reliability marker driven by confirmation thresholds. */
@Composable
fun ReliabilityStars(confirmedCount: Int) {
    Row {
        listOf(5, 10, 20).forEach { threshold ->
            Icon(
                imageVector = if (confirmedCount >= threshold) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = QfGold,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}
