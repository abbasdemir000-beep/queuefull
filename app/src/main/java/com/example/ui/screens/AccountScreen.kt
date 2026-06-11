package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.usecase.AuthPolicy
import com.example.ui.QueueFuelViewModel
import com.example.ui.components.QfCard
import com.example.ui.components.QfStatusBadge
import com.example.ui.theme.*

/**
 * Account — profile summary plus the doorways to every secondary surface:
 * alerts, station/city suggestions, the admin panel (admin only), the GPS
 * simulation controls, and the dev role switcher. Nothing was dropped from
 * the previous five-tab layout; it now lives here, decluttered.
 */
@Composable
fun AccountScreen(
    viewModel: QueueFuelViewModel,
    onOpenAlerts: () -> Unit,
    onOpenSuggestions: () -> Unit,
    onOpenAdmin: () -> Unit
) {
    val user = viewModel.currentUser ?: return
    val notifications by viewModel.notifications.collectAsState()
    var showDevTools by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("account_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Profile header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(QfDeepTeal, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.take(2).uppercase(),
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    color = QfTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "${user.phoneNumber} • ${user.city}",
                    color = QfTextSecondary,
                    fontSize = 12.sp
                )
            }
            QfStatusBadge(
                label = when (user.role) {
                    "ADMIN" -> "مشرف"
                    "REPORTER" -> "مراسل"
                    else -> "مواطن"
                },
                color = if (user.role == "ADMIN") QfGold else QfTurquoise
            )
        }

        // Points strip
        QfCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("رصيد النقاط", color = QfTextSecondary, fontSize = 13.sp)
                Text(
                    text = "${user.points} نقطة",
                    color = QfGold,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Navigation rows
        AccountNavRow(
            icon = Icons.Filled.Notifications,
            title = "التنبيهات وسجل التحديثات",
            badgeCount = notifications.size,
            onClick = onOpenAlerts,
            testTag = "account_alerts_row"
        )
        AccountNavRow(
            icon = Icons.Filled.AddLocationAlt,
            title = "اقتراح محطة أو مدينة جديدة",
            onClick = onOpenSuggestions,
            testTag = "account_suggestions_row"
        )
        if (user.role == "ADMIN") {
            AccountNavRow(
                icon = Icons.Filled.AdminPanelSettings,
                title = "لوحة تحكم الإشراف",
                tint = QfGold,
                onClick = onOpenAdmin,
                testTag = "account_admin_row"
            )
        }

        // GPS simulation (MVP location source) — collapsed into one card
        QfCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.MyLocation, contentDescription = null, tint = QfTurquoise, modifier = Modifier.size(20.dp))
                        Text("محاكاة الموقع (GPS)", color = QfTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Switch(
                        checked = viewModel.simulateGpsEnabled,
                        onCheckedChange = { viewModel.simulateGpsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = QfDeepTeal
                        )
                    )
                }
                Text(
                    text = if (viewModel.simulateGpsEnabled)
                        "التقارير تتطلب التواجد ضمن 200م من المكان. اختر موقعك المحاكى:"
                    else
                        "فحص القرب الجغرافي معطّل — يمكنك التقرير من أي مكان (وضع التطوير).",
                    color = if (viewModel.simulateGpsEnabled) QfTextSecondary else QfWarning,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
                if (viewModel.simulateGpsEnabled) {
                    val presets = listOf(
                        Triple("كركوك", 35.4682, 44.3921),
                        Triple("أربيل", 36.1915, 44.0094),
                        Triple("السليمانية", 35.5601, 45.4208)
                    )
                    presets.forEach { (name, lat, lng) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.simLatitude = lat
                                    viewModel.simLongitude = lng
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = viewModel.simLatitude == lat,
                                onClick = {
                                    viewModel.simLatitude = lat
                                    viewModel.simLongitude = lng
                                }
                            )
                            Text("موقع $name", color = QfTextPrimary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Dev role switcher (testing utility from the previous profile screen)
        QfCard(modifier = Modifier.fillMaxWidth(), containerColor = QfSurfaceVariant) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDevTools = !showDevTools },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🛠️ أدوات التطوير — تبديل الصلاحية",
                        color = QfGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(if (showDevTools) "إخفاء" else "عرض", color = QfTextTertiary, fontSize = 11.sp)
                }
                if (showDevTools) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RoleButton("مواطن", "USER", user.role, viewModel, Modifier.weight(1f))
                        RoleButton("مراسل", "REPORTER", user.role, viewModel, Modifier.weight(1f))
                        if (AuthPolicy.isAdminPhone(user.phoneNumber)) {
                            RoleButton("مشرف", "ADMIN", user.role, viewModel, Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Logout
        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = QfSurfaceVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("logout_button")
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = QfTextPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("تسجيل الخروج", color = QfTextPrimary, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun AccountNavRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    badgeCount: Int = 0,
    tint: Color = QfTurquoise,
    testTag: String = ""
) {
    QfCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(QfSurfaceVariant, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Text(
                text = title,
                color = QfTextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (badgeCount > 0) {
                Badge { Text(badgeCount.toString()) }
            }
            Icon(
                imageVector = Icons.Filled.ChevronLeft,
                contentDescription = null,
                tint = QfTextTertiary
            )
        }
    }
}

@Composable
private fun RoleButton(
    label: String,
    role: String,
    currentRole: String,
    viewModel: QueueFuelViewModel,
    modifier: Modifier = Modifier
) {
    val active = currentRole == role
    Button(
        onClick = { viewModel.switchRole(role) },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (active) QfDeepTeal else QfSurface,
            contentColor = if (active) Color.White else QfTextSecondary
        ),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        modifier = modifier.height(36.dp)
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}
