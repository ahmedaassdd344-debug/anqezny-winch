package com.example.ui.components

import android.preference.PreferenceManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.GIZA_AREAS
import com.example.ui.theme.AnqeznyCharcoal
import com.example.ui.theme.AnqeznyCrimson
import com.example.ui.theme.AnqeznyEmerald
import com.example.ui.theme.AnqeznyInk
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import kotlin.math.roundToInt

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
    val context = LocalContext.current
    remember {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
        Configuration.getInstance().userAgentValue = context.packageName
        true
    }

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
                .height(270.dp)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(13.5)
                        controller.setCenter(GeoPoint(pickupLat, pickupLng))
                    }
                },
                update = { mapView ->
                    mapView.overlays.clear()

                    val pPoint = GeoPoint(pickupLat, pickupLng)
                    val dPoint = GeoPoint(dropoffLat, dropoffLng)

                    val eventsReceiver = object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                            if (p != null && selectionMode != MapSelectionMode.NONE && onMapTap != null) {
                                val nearest = GIZA_AREAS.minByOrNull { area ->
                                    val dLat = area.lat - p.latitude
                                    val dLng = area.lng - p.longitude
                                    dLat * dLat + dLng * dLng
                                }
                                val label = nearest?.let { "بالقرب من ${it.nameAr}" } ?: "موقع محدد على الخريطة"
                                onMapTap(
                                    (p.latitude * 10000).roundToInt() / 10000.0,
                                    (p.longitude * 10000).roundToInt() / 10000.0,
                                    label
                                )
                                return true
                            }
                            return false
                        }
                        override fun longPressHelper(p: GeoPoint?): Boolean = false
                    }
                    mapView.overlays.add(MapEventsOverlay(eventsReceiver))

                    val line = Polyline().apply {
                        setPoints(listOf(pPoint, dPoint))
                        outlinePaint.color = if (isLoadedPhase) android.graphics.Color.RED else android.graphics.Color.DKGRAY
                        outlinePaint.strokeWidth = 9f
                    }
                    mapView.overlays.add(line)

                    val pickupMarker = Marker(mapView).apply {
                        position = pPoint
                        title = "موقع الاستلام (أ): $pickupLabel"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    mapView.overlays.add(pickupMarker)

                    val dropoffMarker = Marker(mapView).apply {
                        position = dPoint
                        title = "وجهة النقل (ب): $dropoffLabel"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    mapView.overlays.add(dropoffMarker)

                    mapView.controller.animateTo(
                        if (selectionMode == MapSelectionMode.DROPOFF) dPoint else pPoint
                    )
                    mapView.invalidate()
                }
            )

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
                            "المس أي شارع لتحديد موقع الاستلام (أ)"
                        } else {
                            "المس أي شارع لتحديد وجهة النقل (ب)"
                        },
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }

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
