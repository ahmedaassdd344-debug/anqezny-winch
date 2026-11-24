package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DriverProfile
import com.example.data.model.DriverReview
import com.example.ui.theme.AnqeznyCharcoal
import com.example.ui.theme.AnqeznyCrimson
import com.example.ui.theme.AnqeznyEmerald
import com.example.ui.theme.AnqeznyEmeraldContainer
import com.example.ui.theme.AnqeznyInk
import com.example.ui.theme.AnqeznyInkSecondary
import com.example.ui.theme.AnqeznyStarGold
import com.example.ui.theme.AnqeznySurfaceVariant
import com.example.ui.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DriverProfileSheet(
    driver: DriverProfile,
    reviewsState: UiState<List<DriverReview>>,
    onDismiss: () -> Unit
) {
    BackHandler { onDismiss() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 620.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(AnqeznyCharcoal),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = "أيقونة السائق",
                                tint = Color.White
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = driver.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = AnqeznyInk,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "موثق",
                                    tint = AnqeznyEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "${driver.winchType} • لوحة: ${driver.plateNumber}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AnqeznyInkSecondary
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_driver_profile_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Overall rating & stats summary card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AnqeznySurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (driver.ratingCount > 0) String.format(Locale.US, "%.1f", driver.ratingAvg) else "جديد",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = AnqeznyInk,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = AnqeznyStarGold,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Text(
                                text = "${driver.ratingCount} تقييم • ${driver.totalTrips} رحلة",
                                style = MaterialTheme.typography.labelMedium,
                                color = AnqeznyInkSecondary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AnqeznyEmeraldContainer
                            ) {
                                Text(
                                    text = "نطاق التشغيل: ${driver.area}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AnqeznyEmerald,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            if (!driver.companyName.isNullOrBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "شركة: ${driver.companyName}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AnqeznyInkSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                when (reviewsState) {
                    is UiState.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = AnqeznyCrimson)
                        }
                    }
                    is UiState.Error -> {
                        Text(
                            text = reviewsState.message,
                            color = AnqeznyCrimson,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    is UiState.Success -> {
                        val reviews = reviewsState.data.sortedByDescending { it.createdAt?.seconds ?: 0L }

                        // 5-to-1 Star Distribution Bars
                        if (reviews.isNotEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                for (star in 5 downTo 1) {
                                    val count = reviews.count { it.stars.toInt() == star }
                                    val ratio = count.toFloat() / reviews.size.toFloat()
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "$star ★",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = AnqeznyInk,
                                            modifier = Modifier.width(34.dp)
                                        )
                                        LinearProgressIndicator(
                                            progress = { ratio },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(7.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            color = AnqeznyStarGold,
                                            trackColor = AnqeznySurfaceVariant
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "$count",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = AnqeznyInkSecondary,
                                            modifier = Modifier.width(24.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                        }

                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = "آراء العملاء (${reviews.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = AnqeznyInk,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))

                        if (reviews.isEmpty()) {
                            Text(
                                text = "لا توجد تعليقات مكتوبة حتى الآن لهذا السائق.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AnqeznyInkSecondary,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(reviews, key = { it.id }) { rev ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF8FAFC),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = rev.customerName,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = AnqeznyInk,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    repeat(rev.stars.toInt().coerceIn(1, 5)) {
                                                        Icon(
                                                            imageVector = Icons.Default.Star,
                                                            contentDescription = null,
                                                            tint = AnqeznyStarGold,
                                                            modifier = Modifier.size(15.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            if (rev.tags.isNotEmpty()) {
                                                Spacer(Modifier.height(4.dp))
                                                FlowRow(
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    rev.tags.forEach { tag ->
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = Color.White
                                                        ) {
                                                            Text(
                                                                text = tag,
                                                                style = MaterialTheme.typography.labelMedium,
                                                                color = AnqeznyInkSecondary,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                            if (rev.comment.isNotBlank()) {
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    text = rev.comment,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = AnqeznyInk
                                                )
                                            }
                                            rev.createdAt?.let { ts ->
                                                Spacer(Modifier.height(2.dp))
                                                val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale("ar")).format(ts.toDate())
                                                Text(
                                                    text = dateStr,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = AnqeznyInkSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
