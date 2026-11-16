package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MandiGreenPrimary
import com.example.ui.theme.MandiLeafSecondary
import com.example.ui.theme.MandiNumberFont
import com.example.ui.theme.VegAdrak
import com.example.ui.theme.VegAloo
import com.example.ui.theme.VegBaingan
import com.example.ui.theme.VegBhindi
import com.example.ui.theme.VegGobi
import com.example.ui.theme.VegKarela
import com.example.ui.theme.VegLehsan
import com.example.ui.theme.VegPalak
import com.example.ui.theme.VegPyaz
import com.example.ui.theme.VegTamatar
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

fun formatRs(amount: Double): String {
    val rounded = kotlin.math.round(amount * 100.0) / 100.0
    val isNegative = rounded < 0
    val absVal = abs(rounded)
    val nf = NumberFormat.getNumberInstance(Locale.US)
    val formatted = if (absVal % 1.0 == 0.0) {
        nf.maximumFractionDigits = 0
        nf.format(absVal.toLong())
    } else {
        nf.minimumFractionDigits = 2
        nf.maximumFractionDigits = 2
        nf.format(absVal)
    }
    return if (isNegative) "-Rs. $formatted" else "Rs. $formatted"
}

fun formatKg(kg: Double): String {
    val rounded = kotlin.math.round(kg * 100.0) / 100.0
    val numText = if (rounded % 1.0 == 0.0) {
        rounded.toLong().toString()
    } else {
        rounded.toString()
    }
    return "$numText kg"
}

fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun formatDateHeader(timestamp: Long = System.currentTimeMillis()): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

fun vegetableAccentColor(name: String): Color {
    return when (name.trim().lowercase()) {
        "aloo" -> VegAloo
        "pyaz" -> VegPyaz
        "tamatar" -> VegTamatar
        "adrak" -> VegAdrak
        "lehsan" -> VegLehsan
        "bhindi" -> VegBhindi
        "karela" -> VegKarela
        "palak" -> VegPalak
        "gobi" -> VegGobi
        "baingan" -> VegBaingan
        else -> MandiLeafSecondary
    }
}

@Composable
fun MandiTopHeader(
    title: String,
    subtitle: String,
    badgeLabel: String? = null,
    badgeValue: String? = null,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(MandiGreenPrimary, MandiLeafSecondary),
                ),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            )
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
            if (badgeLabel != null && badgeValue != null) {
                Surface(
                    color = Color.White.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.End,
                    ) {
                        Text(
                            text = badgeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f),
                        )
                        Text(
                            text = badgeValue,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = MandiNumberFont,
                            ),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MandiEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
