package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GIZA_AREAS
import com.example.ui.theme.AnqeznyCharcoal
import com.example.ui.theme.AnqeznyCrimson
import com.example.ui.theme.AnqeznyEmerald
import com.example.ui.theme.AnqeznyInk
import com.example.ui.theme.AnqeznyInkSecondary
import kotlin.math.roundToInt

// Bounding box for Greater Giza service corridor
private const val MIN_LAT = 29.90
private const val MAX_LAT = 30.15
private const val MIN_LNG = 30.90
private const val MAX_LNG = 31.25

enum class MapSelectionMode {
    NONE, PICKUP, DROPOFF
}

@Composable
fun GizaRouteMap(
    pickupLat: Double,
    pickupLng: Double,
    pickupLabel: String,
    dropoffLat: Double,
    dropoffLng: Double,
    dropoffLabel: String,
    distanceKm: Double,
    selectionMode: MapSelectionMode = MapSelectionMode.NONE,
    tripStage: String? = null,
    onMapTap: ((lat: Double, lng: Double, nearestAreaName: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val infiniteTransition = rememberInfiniteTransition(label = "truck_pulse")
    val truckProgress by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "truck_progress"
    )

    val isLoadedPhase = tripStage == "loaded"
    val stageBanner = when (tripStage) {
        "accepted" -> "السائق يتجه الآن نحو موقع الاستلام"
        "arrived" -> "السائق وصل موقع الاستلام — جاري المعاينة"
        "loaded" -> "تم التحميل — التحرك الآن نحو وجهة النقل: $dropoffLabel"
        "completed" -> "تم الوصول والتسليم في $dropoffLabel"
        else -> null
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            .background(Color(0xFFF1F5F9))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .pointerInput(selectionMode, onMapTap) {
                    if (selectionMode != MapSelectionMode.NONE && onMapTap != null) {
                        detectTapGestures { offset ->
                            val lng = MIN_LNG + (offset.x / size.width) * (MAX_LNG - MIN_LNG)
                            val lat = MAX_LAT - (offset.y / size.height) * (MAX_LAT - MIN_LAT)
                            val clampedLat = lat.coerceIn(MIN_LAT, MAX_LAT)
                            val clampedLng = lng.coerceIn(MIN_LNG, MAX_LNG)
                            val nearest = GIZA_AREAS.minByOrNull { area ->
                                val dLat = area.lat - clampedLat
                                val dLng = area.lng - clampedLng
                                dLat * dLat + dLng * dLng
                            }
                            val label = nearest?.let { "بالقرب من ${it.nameAr}" } ?: "موقع محدد على الخريطة"
                            onMapTap(
                                (clampedLat * 10000).roundToInt() / 10000.0,
                                (clampedLng * 10000).roundToInt() / 10000.0,
                                label
                            )
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                fun toOffset(lat: Double, lng: Double): Offset {
                    val x = ((lng - MIN_LNG) / (MAX_LNG - MIN_LNG)).toFloat().coerceIn(0.04f, 0.96f) * w
                    val y = ((MAX_LAT - lat) / (MAX_LAT - MIN_LAT)).toFloat().coerceIn(0.06f, 0.94f) * h
                    return Offset(x, y)
                }

                // Map land background + subtle topographic grid
                drawRect(color = Color(0xFFF8FAFC))
                val gridStep = 36f
                var gx = 0f
                while (gx < w) {
                    drawLine(Color(0xFFE2E8F0), Offset(gx, 0f), Offset(gx, h), strokeWidth = 1f)
                    gx += gridStep
                }
                var gy = 0f
                while (gy < h) {
                    drawLine(Color(0xFFE2E8F0), Offset(0f, gy), Offset(w, gy), strokeWidth = 1f)
                    gy += gridStep
                }

                // Nile River ribbon on the east edge of Giza
                val nilePath = Path().apply {
                    moveTo(w * 0.93f, 0f)
                    cubicTo(w * 0.89f, h * 0.3f, w * 0.94f, h * 0.65f, w * 0.88f, h)
                }
                drawPath(nilePath, color = Color(0xFFBAE6FD), style = Stroke(width = 16f, cap = StrokeCap.Round))

                // Major Giza corridors: Ring Road, 26th of July Mehwar, Haram/Wahat
                val ringRoad = Path().apply {
                    moveTo(w * 0.35f, h * 0.08f)
                    cubicTo(w * 0.68f, h * 0.14f, w * 0.80f, h * 0.55f, w * 0.55f, h * 0.88f)
                }
                drawPath(ringRoad, color = Color(0xFFCBD5E1), style = Stroke(width = 5f))

                // Mehwar (26th July)
                val mehwarStart = toOffset(30.0561, 31.2001)
                val mehwarEnd = toOffset(30.0218, 30.9450)
                drawLine(Color(0xFFCBD5E1), mehwarStart, mehwarEnd, strokeWidth = 5f)

                // Wahat / Haram axis
                val haramStart = toOffset(30.0086, 31.1900)
                val haramEnd = toOffset(29.9350, 30.9400)
                drawLine(Color(0xFFCBD5E1), haramStart, haramEnd, strokeWidth = 5f)

                // 14 Giza Area nodes
                GIZA_AREAS.forEach { area ->
                    val pt = toOffset(area.lat, area.lng)
                    drawCircle(Color(0xFF94A3B8), radius = 4f, center = pt)
                    drawAreaLabel(textMeasurer, area.nameAr, pt, w)
                }

                // Route line between Pickup & Dropoff
                val pStart = toOffset(pickupLat, pickupLng)
                val pEnd = toOffset(dropoffLat, dropoffLng)
                val routeColor = if (isLoadedPhase) AnqeznyCrimson else AnqeznyCharcoal
                val pathEffect = if (tripStage == "accepted" || tripStage == "arrived") {
                    PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)
                } else null

                drawLine(
                    color = routeColor.copy(alpha = 0.25f),
                    start = pStart,
                    end = pEnd,
                    strokeWidth = 10f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = routeColor,
                    start = pStart,
                    end = pEnd,
                    strokeWidth = 5f,
                    cap = StrokeCap.Round,
                    pathEffect = pathEffect
                )

                // Animated moving winch marker when trip is active
                if (tripStage == "accepted" || tripStage == "loaded") {
                    val tx = pStart.x + (pEnd.x - pStart.x) * truckProgress
                    val ty = pStart.y + (pEnd.y - pStart.y) * truckProgress
                    drawCircle(AnqeznyEmerald.copy(alpha = 0.28f), radius = 22f, center = Offset(tx, ty))
                    drawCircle(AnqeznyEmerald, radius = 11f, center = Offset(tx, ty))
                    drawCircle(Color.White, radius = 5f, center = Offset(tx, ty))
                }

                // Pickup Pin ("أ" Charcoal)
                drawCircle(Color.White, radius = 18f, center = pStart)
                drawCircle(AnqeznyCharcoal, radius = 14f, center = pStart)
                drawPinLetter(textMeasurer, "أ", pStart)

                // Dropoff Pin ("ب" Crimson)
                drawCircle(Color.White, radius = 18f, center = pEnd)
                drawCircle(AnqeznyCrimson, radius = 14f, center = pEnd)
                drawPinLetter(textMeasurer, "ب", pEnd)
            }

            // Distance badge top-start
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp),
                shape = RoundedCornerShape(12.dp),
                color = AnqeznyCharcoal.copy(alpha = 0.92f)
            ) {
                Text(
                    text = "المسافة: ${distanceKm} كم",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            // Active selection prompt badge
            if (selectionMode != MapSelectionMode.NONE) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(10.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = if (selectionMode == MapSelectionMode.PICKUP) AnqeznyCharcoal else AnqeznyCrimson
                ) {
                    Text(
                        text = if (selectionMode == MapSelectionMode.PICKUP) {
                            "انقر على الخريطة لتحديد موقع الاستلام (أ)"
                        } else {
                            "انقر على الخريطة لتحديد وجهة النقل (ب)"
                        },
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }

        // Route summary strip
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(AnqeznyCharcoal),
                    contentAlignment = Alignment.Center
                ) {
                    Text("أ", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "من: $pickupLabel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AnqeznyInk,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(AnqeznyCrimson),
                    contentAlignment = Alignment.Center
                ) {
                    Text("ب", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "إلى: $dropoffLabel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AnqeznyInk,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (stageBanner != null) {
                Spacer(Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isLoadedPhase) Color(0xFFFEE2E2) else Color(0xFFD1FAE5))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (isLoadedPhase) Icons.Default.LocalShipping else Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (isLoadedPhase) AnqeznyCrimson else AnqeznyEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stageBanner,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isLoadedPhase) AnqeznyCrimson else AnqeznyEmerald,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawAreaLabel(
    measurer: TextMeasurer,
    text: String,
    pt: Offset,
    canvasWidth: Float
) {
    val result = measurer.measure(
        text = text,
        style = TextStyle(color = AnqeznyInkSecondary, fontSize = 9.sp, fontWeight = FontWeight.Medium)
    )
    val left = (pt.x - result.size.width / 2f).coerceIn(4f, canvasWidth - result.size.width - 4f)
    val top = (pt.y + 6f).coerceIn(4f, size.height - result.size.height - 4f)
    drawText(result, topLeft = Offset(left, top))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPinLetter(
    measurer: TextMeasurer,
    letter: String,
    center: Offset
) {
    val result = measurer.measure(
        text = letter,
        style = TextStyle(color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    )
    drawText(
        result,
        topLeft = Offset(center.x - result.size.width / 2f, center.y - result.size.height / 2f)
    )
}
