package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HomeRepairService
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.QueueCategory
import com.example.ui.theme.*

// ============================================================================
// QueueFuel component library — every screen is assembled from these pieces
// so the whole app reads as one product family. See docs/DESIGN_SYSTEM.md.
// ============================================================================

/** Master brand mark: teal location pin + white people queue on a soft glow. */
@Composable
fun QueueFuelLogo(size: Dp = 80.dp, glow: Boolean = true, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(size * 1.5f),
        contentAlignment = Alignment.Center
    ) {
        if (glow) {
            Box(
                modifier = Modifier
                    .size(size * 1.5f)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(QfTurquoise.copy(alpha = 0.22f), Color.Transparent)
                        ),
                        CircleShape
                    )
            )
        }
        Image(
            painter = painterResource(R.drawable.ic_qf_logo),
            contentDescription = "QueueFuel",
            modifier = Modifier.size(size)
        )
    }
}

/** Standard card: dark blue surface, 16dp radius, hairline border. */
@Composable
fun QfCard(
    modifier: Modifier = Modifier,
    borderColor: Color = QfBorder,
    containerColor: Color = QfSurface,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, borderColor),
        content = content
    )
}

/** Section header used above every list/section on a screen. */
@Composable
fun QfSectionHeader(title: String, trailing: String? = null, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = QfTextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
        if (trailing != null) {
            Text(text = trailing, color = QfTextTertiary, fontSize = 11.sp)
        }
    }
}

/** Pill badge carrying a queue-status color (cards, map legend, admin rows). */
@Composable
fun QfStatusBadge(label: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

/** Icon for each queue category (UI-layer mapping of the domain enum). */
fun QueueCategory.icon(): ImageVector = when (this) {
    QueueCategory.FUEL -> Icons.Filled.LocalGasStation
    QueueCategory.GOVERNMENT -> Icons.Filled.AccountBalance
    QueueCategory.HOSPITALS -> Icons.Filled.LocalHospital
    QueueCategory.BANKS -> Icons.Filled.Savings
    QueueCategory.UNIVERSITIES -> Icons.Filled.School
    QueueCategory.PASSPORT -> Icons.Filled.Badge
    QueueCategory.SERVICE_CENTERS -> Icons.Filled.HomeRepairService
    QueueCategory.MORE -> Icons.Filled.Apps
}

/** One tile of the home-screen categories grid. */
@Composable
fun QfCategoryTile(
    category: QueueCategory,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) QfTealContainer else QfSurface)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp)
            .testTag("category_tile_${category.name}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (selected) QfDeepTeal else QfSurfaceVariant,
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon(),
                contentDescription = category.labelAr,
                tint = if (selected) Color.White else QfTurquoise,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = category.labelAr,
            color = if (selected) QfOnTealContainer else QfTextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        if (category.comingSoon) {
            Text(
                text = "قريباً",
                color = QfTextTertiary,
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium
            )
        } else {
            Text(
                text = "متاح الآن",
                color = QfSuccess,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** Home-screen categories grid (4 × 2), RTL-aware via Compose layout. */
@Composable
fun QfCategoriesGrid(
    selectedCategory: QueueCategory,
    onCategoryClick: (QueueCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = QueueCategory.entries
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.chunked(4).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { category ->
                    QfCategoryTile(
                        category = category,
                        selected = category == selectedCategory,
                        onClick = { onCategoryClick(category) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/** Rounded dark search field used at the top of the home screen. */
@Composable
fun QfSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = QfTextTertiary, fontSize = 13.sp) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = QfTextSecondary) },
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = QfTextPrimary,
            unfocusedTextColor = QfTextPrimary,
            focusedContainerColor = QfSurfaceVariant,
            unfocusedContainerColor = QfSurfaceVariant,
            focusedBorderColor = QfDeepTeal,
            unfocusedBorderColor = Color.Transparent
        )
    )
}

/** Compact selectable chip — the only filter affordance on the home screen. */
@Composable
fun QfChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = QfDeepTeal
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) selectedColor else QfSurfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else QfTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Stepper for the report wizard: numbered circles joined by connector lines,
 * teal for done/current steps — the progress style of the design board.
 */
@Composable
fun QfWizardProgress(
    stepCount: Int,
    currentStep: Int, // 1-based
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (step in 1..stepCount) {
            val reached = step <= currentStep
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(
                        if (reached) QfDeepTeal else QfSurfaceVariant,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (step < currentStep) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        text = step.toString(),
                        color = if (reached) Color.White else QfTextTertiary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (step < stepCount) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .background(if (step < currentStep) QfDeepTeal else QfSurfaceVariant)
                )
            }
        }
    }
}

/** Top bar for pushed sub-screens (details, alerts, suggestions, admin…). */
@Composable
fun QfSubScreenTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(QfSurface)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack, modifier = Modifier.testTag("subscreen_back")) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "رجوع",
                tint = QfTextPrimary
            )
        }
        Text(
            text = title,
            color = QfTextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        actions()
    }
}
