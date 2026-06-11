package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.Station
import com.example.ui.QueueFuelViewModel
import com.example.ui.components.QfCard
import com.example.ui.components.QfChip
import com.example.ui.components.QfSubScreenTopBar
import com.example.ui.components.QfWizardProgress
import com.example.ui.theme.*

/**
 * Report wizard — the design board's four-step flow with a stepper on top:
 *   1. confirm GPS/location (pick or confirm the place)
 *   2. choose queue status: فارغ / متوسط / مزدحم (+ fuel availability/type)
 *   3. take/upload the mandatory photo
 *   4. AI verification result + points/raffle outcome
 * Submission still runs through QueueFuelViewModel.submitStatusUpdate, so the
 * proximity check, photo requirement, verification, points, and raffle entry
 * behave exactly as before.
 */
@Composable
fun ReportWizardScreen(
    viewModel: QueueFuelViewModel,
    initialStation: Station?,
    onClose: () -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var targetStation by remember { mutableStateOf(initialStation) }
    var selectedStatus by remember { mutableStateOf("EMPTY") }
    var hasFuel by remember { mutableStateOf(true) }
    var selectedFuelKind by remember { mutableStateOf("عادي") }
    var photoPath by remember { mutableStateOf<String?>(null) }
    var submitted by remember { mutableStateOf(false) }

    // Clear any stale verification outcome from a previous report
    LaunchedEffect(Unit) { viewModel.lastReportResult = null }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QfNavy)
            .navigationBarsPadding()
            .testTag("report_wizard_screen")
    ) {
        QfSubScreenTopBar(title = "تقرير جديد", onBack = onClose)
        QfWizardProgress(stepCount = 4, currentStep = step)

        when (step) {
            1 -> WizardStepLocation(
                viewModel = viewModel,
                targetStation = targetStation,
                onStationSelected = { targetStation = it },
                onNext = { step = 2 }
            )
            2 -> WizardStepStatus(
                selectedStatus = selectedStatus,
                onStatusSelected = { selectedStatus = it },
                hasFuel = hasFuel,
                onHasFuelChange = { hasFuel = it },
                selectedFuelKind = selectedFuelKind,
                onFuelKindSelected = { selectedFuelKind = it },
                onNext = { step = 3 },
                onPrevious = { step = 1 }
            )
            3 -> WizardStepPhoto(
                photoPath = photoPath,
                onTakePhoto = { cameraLauncher.launch(null) },
                onPickPhoto = { galleryLauncher.launch("image/*") },
                onNext = {
                    val station = targetStation
                    if (station != null && !submitted) {
                        submitted = true
                        viewModel.submitStatusUpdate(
                            stationId = station.id,
                            newQueueStatus = selectedStatus,
                            hasFuel = hasFuel,
                            selectedFuel = selectedFuelKind,
                            photoPath = photoPath
                        )
                    }
                    step = 4
                },
                onPrevious = { step = 2 }
            )
            4 -> WizardStepResult(
                viewModel = viewModel,
                onDone = onClose
            )
        }
    }
}

// ---------------- Step 1 — confirm GPS / pick the place ----------------
@Composable
private fun WizardStepLocation(
    viewModel: QueueFuelViewModel,
    targetStation: Station?,
    onStationSelected: (Station) -> Unit,
    onNext: () -> Unit
) {
    // Subscribing here starts the WhileSubscribed station stream and recomposes
    // this step on Room emissions; filteredStationsList reads the same value.
    val approvedStations by viewModel.approvedStations.collectAsState()
    val nearestStations = (if (approvedStations.isEmpty()) emptyList() else viewModel.filteredStationsList)
        .sortedBy { st ->
            viewModel.calculateDistance(
                viewModel.currentLatitude, viewModel.currentLongitude,
                st.latitude, st.longitude
            )
        }
    val canReport = targetStation != null &&
        (!viewModel.simulateGpsEnabled || viewModel.isUserNear(targetStation))

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            WizardStepTitle(
                title = "تأكيد الموقع",
                subtitle = "اختر المكان الذي تقف في طابوره الآن — يجب أن تكون ضمن 200م."
            )

            // GPS source row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(QfSurface, RoundedCornerShape(12.dp))
                    .border(1.dp, QfBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.MyLocation, contentDescription = null, tint = QfTurquoise, modifier = Modifier.size(18.dp))
                    Text("التحقق الجغرافي (GPS)", color = QfTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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

            Spacer(modifier = Modifier.height(10.dp))
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            items(nearestStations) { station ->
                val distance = viewModel.calculateDistance(
                    viewModel.currentLatitude, viewModel.currentLongitude,
                    station.latitude, station.longitude
                )
                val selected = targetStation?.id == station.id
                val near = !viewModel.simulateGpsEnabled || viewModel.isUserNear(station)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (selected) QfTealContainer else QfSurface)
                        .border(
                            1.dp,
                            if (selected) QfDeepTeal else QfBorder,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onStationSelected(station) }
                        .padding(12.dp)
                        .testTag("wizard_station_${station.id}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalGasStation,
                        contentDescription = null,
                        tint = if (selected) QfOnTealContainer else QfTextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = station.name,
                            color = QfTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "يبعد ${"%.0f".format(distance)}م" + if (near) " — ضمن النطاق ✓" else " — خارج النطاق",
                            color = if (near) QfSuccess else QfWarning,
                            fontSize = 11.sp
                        )
                    }
                    if (selected) {
                        Icon(Icons.Filled.Check, contentDescription = "محدد", tint = QfTurquoise, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        WizardNavButtons(
            nextLabel = "التالي",
            nextEnabled = canReport,
            onNext = onNext,
            helper = when {
                targetStation == null -> "اختر مكاناً للمتابعة"
                !canReport -> "أنت خارج نطاق 200م لهذا المكان — اقترب منه أو فعّل المحاكاة من حسابك"
                else -> null
            }
        )
    }
}

// ---------------- Step 2 — queue status ----------------
@Composable
private fun WizardStepStatus(
    selectedStatus: String,
    onStatusSelected: (String) -> Unit,
    hasFuel: Boolean,
    onHasFuelChange: (Boolean) -> Unit,
    selectedFuelKind: String,
    onFuelKindSelected: (String) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            WizardStepTitle(
                title = "حالة الطابور",
                subtitle = "كيف يبدو الطابور أمامك الآن؟"
            )

            val options = listOf(
                Triple("EMPTY", "فارغ", QfSuccess),
                Triple("MODERATE", "متوسط", QfWarning),
                Triple("LONG", "مزدحم", QfError)
            )
            options.forEach { (key, label, color) ->
                val selected = selectedStatus == key
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (selected) color.copy(alpha = 0.15f) else QfSurface)
                        .border(
                            1.5.dp,
                            if (selected) color else QfBorder,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onStatusSelected(key) }
                        .padding(14.dp)
                        .testTag("wizard_status_$key"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(color, CircleShape)
                    )
                    Text(
                        text = label,
                        color = if (selected) color else QfTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = estimatedQueueLabel(key),
                        color = QfTextTertiary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fuel availability — category-specific detail (fuel is the live category)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(QfSurface, RoundedCornerShape(12.dp))
                    .border(1.dp, QfBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("هل الوقود متوفر؟", color = QfTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Switch(
                    checked = hasFuel,
                    onCheckedChange = onHasFuelChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = QfDeepTeal
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("نوع الوقود الذي عبأته:", color = QfTextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("عادي", "محسن", "سوبر").forEach { kind ->
                    QfChip(
                        label = kind,
                        selected = selectedFuelKind == kind,
                        onClick = { onFuelKindSelected(kind) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        WizardNavButtons(
            nextLabel = "التالي",
            nextEnabled = true,
            onNext = onNext,
            onPrevious = onPrevious
        )
    }
}

// ---------------- Step 3 — mandatory photo ----------------
@Composable
private fun WizardStepPhoto(
    photoPath: String?,
    onTakePhoto: () -> Unit,
    onPickPhoto: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            WizardStepTitle(
                title = "صورة من الموقع",
                subtitle = "الصورة إلزامية — يتحقق النظام آلياً من مطابقتها للمكان والازدحام."
            )

            if (photoPath != null) {
                AsyncImage(
                    model = java.io.File(photoPath),
                    contentDescription = "صورة التقرير",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, QfBorder, RoundedCornerShape(14.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(QfSurface, RoundedCornerShape(14.dp))
                        .border(1.dp, QfBorder, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = null,
                            tint = QfTextTertiary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("لم تُرفق صورة بعد", color = QfTextTertiary, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onTakePhoto,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("take_photo_button"),
                    border = BorderStroke(1.dp, QfDeepTeal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = QfTurquoise, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (photoPath == null) "التقط صورة" else "إعادة الالتقاط",
                        fontSize = 12.sp, color = QfTurquoise, fontWeight = FontWeight.Bold
                    )
                }
                OutlinedButton(
                    onClick = onPickPhoto,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("pick_photo_button"),
                    border = BorderStroke(1.dp, QfBorder),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null, tint = QfTextSecondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("من المعرض", fontSize = 12.sp, color = QfTextSecondary, fontWeight = FontWeight.Bold)
                }
            }
        }

        WizardNavButtons(
            nextLabel = "إرسال التقرير",
            nextEnabled = photoPath != null,
            onNext = onNext,
            onPrevious = onPrevious,
            helper = if (photoPath == null) "أرفق صورة للمتابعة" else null,
            nextTestTag = "submit_update_dialog_btn"
        )
    }
}

// ---------------- Step 4 — verification result ----------------
@Composable
private fun WizardStepResult(
    viewModel: QueueFuelViewModel,
    onDone: () -> Unit
) {
    val result = viewModel.lastReportResult

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.6f))

        if (result == null) {
            CircularProgressIndicator(color = QfTurquoise, strokeWidth = 3.dp)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "جاري التحقق الآلي من تقريرك...",
                color = QfTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "مطابقة الموقع والصورة ومستوى الازدحام",
                color = QfTextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        } else {
            val color = if (result.verified) QfSuccess else QfWarning
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape)
                    .border(2.dp, color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (result.verified) Icons.Filled.Check else Icons.Filled.Warning,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = if (result.verified) "تم قبول تقريرك!" else "التقرير قيد المراجعة",
                color = QfTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("wizard_result_title")
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (result.verified)
                    "حصلت على +15 نقطة ودخلت سحب جوائز هذا الشهر 🎟️"
                else
                    result.note + "\nتم حفظ التقرير لمراجعة المشرف.",
                color = QfTextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            if (result.verified) {
                Spacer(modifier = Modifier.height(16.dp))
                QfCard {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("رصيدك الآن:", color = QfTextSecondary, fontSize = 13.sp)
                        Text(
                            text = "${viewModel.currentUser?.points ?: 0} نقطة",
                            color = QfGold,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onDone,
            enabled = result != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("wizard_done_button"),
            colors = ButtonDefaults.buttonColors(containerColor = QfDeepTeal),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("تم", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

// ---------------- Shared wizard pieces ----------------
@Composable
private fun WizardStepTitle(title: String, subtitle: String) {
    Text(
        text = title,
        color = QfTextPrimary,
        fontSize = 19.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.padding(top = 4.dp)
    )
    Text(
        text = subtitle,
        color = QfTextSecondary,
        fontSize = 12.sp,
        modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
    )
}

@Composable
private fun WizardNavButtons(
    nextLabel: String,
    nextEnabled: Boolean,
    onNext: () -> Unit,
    onPrevious: (() -> Unit)? = null,
    helper: String? = null,
    nextTestTag: String = "wizard_next_button"
) {
    Column(modifier = Modifier.padding(16.dp)) {
        if (helper != null) {
            Text(
                text = helper,
                color = QfWarning,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (onPrevious != null) {
                OutlinedButton(
                    onClick = onPrevious,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    border = BorderStroke(1.dp, QfBorder),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("السابق", color = QfTextSecondary, fontWeight = FontWeight.Bold)
                }
            }
            Button(
                onClick = onNext,
                enabled = nextEnabled,
                modifier = Modifier
                    .weight(2f)
                    .height(50.dp)
                    .testTag(nextTestTag),
                colors = ButtonDefaults.buttonColors(
                    containerColor = QfDeepTeal,
                    disabledContainerColor = QfSurfaceVariant
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = nextLabel,
                    color = if (nextEnabled) Color.White else QfTextTertiary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
