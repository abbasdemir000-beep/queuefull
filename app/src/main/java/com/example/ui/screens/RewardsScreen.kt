package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.usecase.ReportVerification
import com.example.ui.QueueFuelViewModel
import com.example.ui.components.QfCard
import com.example.ui.components.QfSectionHeader
import com.example.ui.components.QfStatusBadge
import com.example.ui.formatTime
import com.example.ui.theme.*

/**
 * Rewards — trophy visual, current points, raffle entry status, and the
 * user's report history (each verified report = +15 pts and a raffle entry).
 */
@Composable
fun RewardsScreen(viewModel: QueueFuelViewModel) {
    val user = viewModel.currentUser
    val allUpdates by viewModel.allQueueUpdates.collectAsState()
    val allStations by viewModel.allStations.collectAsState()

    val myUpdates = allUpdates
        .filter { it.userPhone == user?.phoneNumber }
        .sortedByDescending { it.timestamp }
    val verifiedCount = myUpdates.count { it.verification == ReportVerification.VERIFIED }
    val cycleVerified = myUpdates.count {
        it.verification == ReportVerification.VERIFIED && it.timestamp >= viewModel.cycleStartTime
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("rewards_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Trophy + points hero
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(QfGold.copy(alpha = 0.25f), Color.Transparent)
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(QfGold.copy(alpha = 0.15f), CircleShape)
                            .border(2.dp, QfGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = "المكافآت",
                            tint = QfGold,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "${user?.points ?: 0}",
                    color = QfGold,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.testTag("rewards_points")
                )
                Text(
                    text = "نقطة في رصيدك",
                    color = QfTextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        // Raffle entry status
        item {
            QfCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                borderColor = if (cycleVerified > 0) QfDeepTeal else QfBorder
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(QfTealContainer, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ConfirmationNumber,
                            contentDescription = null,
                            tint = QfOnTealContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (cycleVerified > 0) "أنت مشارك في السحب 🎟️" else "لم تدخل السحب بعد",
                            color = QfTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (cycleVerified > 0)
                                "$cycleVerified تقرير موثق في الدورة الحالية"
                            else
                                "قدّم تقريراً موثقاً بالصورة لتدخل سحب الجوائز",
                            color = QfTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Cycle countdown + leaderboard
        item {
            QfCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "الوقت المتبقي على دورة الجوائز الحالية",
                        color = QfTextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = viewModel.timeRemainingString,
                        color = QfGold,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.showFeedbackRewardDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = QfDeepTeal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("rewards_leaderboard_button")
                    ) {
                        Text("🏆 عرض المتنافسين ونقاطهم", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Points history (user's reports)
        item {
            QfSectionHeader(
                title = "سجل نقاطي",
                trailing = "$verifiedCount تقرير موثق"
            )
        }

        if (myUpdates.isEmpty()) {
            item {
                Text(
                    text = "لا توجد تقارير بعد — قدّم أول تقرير لتبدأ بجمع النقاط!",
                    color = QfTextTertiary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                )
            }
        } else {
            items(myUpdates) { update ->
                val stationName = allStations.find { it.id == update.stationId }?.name
                    ?: "مكان محذوف"
                val (label, color, points) = when (update.verification) {
                    ReportVerification.VERIFIED -> Triple("موثق", QfSuccess, "+15")
                    ReportVerification.REJECTED -> Triple("مرفوض", QfError, "0")
                    else -> Triple("قيد المراجعة", QfWarning, "—")
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(QfSurface, RoundedCornerShape(14.dp))
                        .border(1.dp, QfBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stationName,
                            color = QfTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatTime(update.timestamp),
                            color = QfTextTertiary,
                            fontSize = 10.sp
                        )
                    }
                    QfStatusBadge(label = label, color = color)
                    Text(
                        text = points,
                        color = if (update.verification == ReportVerification.VERIFIED) QfGold else QfTextTertiary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}
