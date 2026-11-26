package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.model.AppPricingConfig
import com.example.data.model.DriverPrivateDocs
import com.example.data.model.DriverProfile
import com.example.data.model.DriverReview
import com.example.data.model.Trip
import com.example.data.model.TripMessage
import com.example.data.model.WalletTransaction
import com.example.ui.components.GizaRouteMap
import com.example.ui.components.SignaturePad
import com.example.ui.components.TermsDialog
import com.example.ui.theme.AnqeznyAmber
import com.example.ui.theme.AnqeznyAmberContainer
import com.example.ui.theme.AnqeznyBackground
import com.example.ui.theme.AnqeznyCharcoal
import com.example.ui.theme.AnqeznyCrimson
import com.example.ui.theme.AnqeznyCrimsonContainer
import com.example.ui.theme.AnqeznyEmerald
import com.example.ui.theme.AnqeznyEmeraldContainer
import com.example.ui.theme.AnqeznyInk
import com.example.ui.theme.AnqeznyInkSecondary
import com.example.ui.theme.AnqeznyStarGold
import com.example.ui.theme.AnqeznySurfaceVariant
import com.example.ui.viewmodel.UiState
import com.example.util.createTempCameraUri
import com.example.util.decodeBase64Bitmap
import com.example.util.encodeUriToBase64Thumb
import com.example.util.playNewTripAlert
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun DriverScreen(
    driver: DriverProfile?,
    driverPrivateDocs: DriverPrivateDocs?,
    myReviewsState: UiState<List<DriverReview>>,
    pricingConfig: AppPricingConfig,
    pendingTripsState: UiState<List<Trip>>,
    driverTripsState: UiState<List<Trip>>,
    walletTxState: UiState<List<WalletTransaction>>,
    tripMessagesState: UiState<List<TripMessage>>,
    isBusy: Boolean,
    onWatchTripMessages: (String?) -> Unit,
    onToggleOnline: (Boolean) -> Unit,
    onAcceptTrip: (String, DriverProfile) -> Unit,
    onMarkArrived: (String) -> Unit,
    onSavePhotosAndPin: (String, List<String>, Boolean) -> Unit,
    onCompleteTrip: (Trip, DriverProfile, Int) -> Unit,
    onCancelTrip: (Trip, Double, String) -> Unit,
    onSendMessage: (Trip, String, String, String) -> Unit,
    onRequestWalletTx: (DriverProfile, String, Double, String, String) -> Unit,
    onResubmitDocs: () -> Unit,
    onSwitchToCustomer: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showTerms by remember { mutableStateOf(false) }

    if (showTerms) {
        TermsDialog(onDismiss = { showTerms = false })
    }

        if (driver == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(AnqeznyBackground),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LinearProgressIndicator(color = AnqeznyCrimson)
            Spacer(Modifier.height(12.dp))
            Text("جاري فتح لوحة السائق المعتمد...", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

        if (driver.status != "approved") {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(AnqeznyBackground)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (driver.status == "rejected") Icons.Default.Warning else Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = if (driver.status == "rejected") AnqeznyCrimson else AnqeznyAmber,
                        modifier = Modifier.size(52.dp)
                    )
                    Text(
                        text = when (driver.status) {
                            "rejected" -> "تم رفض المستندات"
                            "suspended" -> "الحساب موقوف مؤقتاً"
                            else -> "أوراقك تحت المراجعة"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when (driver.status) {
                            "rejected" -> "سبب الرفض: ${driver.rejectionReason ?: "يرجى إعادة رفع صور واضحة للأوراق القانونية."}"
                            "suspended" -> "يرجى التواصل مع إدارة منصة انقذنى عايز ونش."
                            else -> "تتم الآن مراجعة البطاقة والرخص وصورة الونش من قبل الإدارة. ستتحدث الشاشة تلقائياً فور التوثيق."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = AnqeznyInkSecondary
                    )
                    if (driver.status == "rejected") {
                        Button(
                            onClick = onResubmitDocs,
                            colors = ButtonDefaults.buttonColors(containerColor = AnqeznyCrimson),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("إعادة رفع الأوراق")
                        }
                    }
                }
            }
        }
        return
    }

    val pendingTrips = (pendingTripsState as? UiState.Success)?.data.orEmpty()
        .sortedByDescending { it.createdAt?.seconds ?: 0L }
    val myTrips = (driverTripsState as? UiState.Success)?.data.orEmpty()
        .sortedByDescending { it.createdAt?.seconds ?: 0L }
    val activeTrip = myTrips.firstOrNull { it.status in listOf("accepted", "arrived", "loaded") }
    val walletTxs = (walletTxState as? UiState.Success)?.data.orEmpty()
        .sortedByDescending { it.createdAt?.seconds ?: 0L }

    // Enforce minimum wallet balance automatically
    val hasMinBalance = driver.walletBalance >= pricingConfig.minWalletEgp
    LaunchedEffect(hasMinBalance, driver.online) {
        if (!hasMinBalance && driver.online) {
            onToggleOnline(false)
        }
    }

    // Sound + Vibration alert when new pending trip arrives while online
    var lastAlertedTripIds by remember { mutableStateOf(setOf<String>()) }
    LaunchedEffect(pendingTrips, driver.online) {
        if (driver.online && activeTrip == null) {
            val newIds = pendingTrips.map { it.id }.toSet()
            if (newIds.any { it !in lastAlertedTripIds } && newIds.isNotEmpty()) {
                playNewTripAlert(context)
            }
            lastAlertedTripIds = newIds
        }
    }

    LaunchedEffect(activeTrip?.id) {
        onWatchTripMessages(activeTrip?.id)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AnqeznyBackground),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Online / Offline Status Strip
        Surface(color = AnqeznyCharcoal, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .widthIn(max = 620.dp)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(driver.name, color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Default.Star, null, tint = AnqeznyStarGold, modifier = Modifier.size(16.dp))
                        Text(
                            text = if (driver.ratingCount > 0) String.format(Locale.US, " %.1f", driver.ratingAvg) else " جديد",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Text(
                        text = "محفظة الضمان: ${driver.walletBalance.roundToInt()} ج.م • لوحة: ${driver.plateNumber}",
                        color = Color(0xFFCBD5E1),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (driver.online) "متصل (Online)" else "غير متصل",
                        color = if (driver.online) Color(0xFF6EE7B7) else Color(0xFFFCA5A5),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = driver.online,
                        onCheckedChange = { target ->
                            if (target && !hasMinBalance) {
                                onShowMessage("الرصيد أقل من الحد الأدنى (${pricingConfig.minWalletEgp.roundToInt()} ج.م) — اشحن المحفظة أولاً")
                                selectedTab = 2
                            } else {
                                onToggleOnline(target)
                            }
                        },
                        modifier = Modifier.testTag("driver_online_switch")
                    )
                }
            }
        }

        if (!hasMinBalance) {
            Surface(
                color = AnqeznyCrimsonContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "رصيد محفظة الضمان أقل من الحد الأدنى (${pricingConfig.minWalletEgp.roundToInt()} ج.م) — تم إيقاف استقبال الطلبات.",
                        color = AnqeznyCrimson,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { selectedTab = 2 }) {
                        Text("شحن الآن", color = AnqeznyCrimson, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        TabRow(selectedTabIndex = selectedTab, containerColor = Color.White, contentColor = AnqeznyCrimson) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("الطلبات") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("مهامي") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("المحفظة") })
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("صفحتي") })
        }

        Column(
            modifier = Modifier
                .widthIn(max = 620.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    if (activeTrip != null) {
                        ActiveDriverMissionCard(
                            trip = activeTrip,
                            driver = driver,
                            pricingConfig = pricingConfig,
                            messages = (tripMessagesState as? UiState.Success)?.data.orEmpty(),
                            isBusy = isBusy,
                            onMarkArrived = { onMarkArrived(activeTrip.id) },
                            onSavePhotosAndPin = { photos, verified ->
                                onSavePhotosAndPin(activeTrip.id, photos, verified)
                            },
                            onCompleteTrip = { sigPoints ->
                                onCompleteTrip(activeTrip, driver, sigPoints)
                            },
                            onCancel = { onCancelTrip(activeTrip, 0.0, "إلغاء طارئ من السائق") },
                            onSendMessage = { msg -> onSendMessage(activeTrip, driver.name, "driver", msg) },
                            onShowMessage = onShowMessage
                        )
                    } else if (!driver.online) {
                        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("أنت الآن غير متصل", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(
                                    "فعّل زر «بدء الوردية (Online)» بالأعلى لاستقبال طلبات الإنقاذ القريبة في الجيزة مع التنبيه الصوتي.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AnqeznyInkSecondary
                                )
                            }
                        }
                    } else if (pendingTrips.isEmpty()) {
                        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("جاري انتظار طلبات جديدة في الجيزة...", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("سيصدر تنبيه صوتي واهتزاز فور وصول أي طلب.", style = MaterialTheme.typography.bodyMedium, color = AnqeznyInkSecondary)
                            }
                        }
                    } else {
                        pendingTrips.forEach { trip ->
                            PendingTripOfferCard(
                                trip = trip,
                                isBusy = isBusy,
                                onAccept = { onAcceptTrip(trip.id, driver) }
                            )
                        }
                    }
                }

                1 -> {
                    val completed = myTrips.filter { it.status == "completed" }
                    val totalGross = completed.sumOf { it.fareEgp }
                    val totalComm = completed.sumOf { it.commissionEgp }
                    val totalNet = completed.sumOf { it.driverNetEgp }

                    Surface(shape = RoundedCornerShape(16.dp), color = AnqeznyCharcoal, modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatCol("الرحلات", "${completed.size}")
                            StatCol("الإجمالي", "${totalGross.roundToInt()} ج.م")
                            StatCol("العمولة", "${totalComm.roundToInt()} ج.م")
                            StatCol("صافي ربحك", "${totalNet.roundToInt()} ج.م")
                        }
                    }

                    if (completed.isEmpty()) {
                        Text("لا توجد مهام مكتملة بعد.", style = MaterialTheme.typography.bodyMedium, color = AnqeznyInkSecondary)
                    } else {
                        completed.forEach { t ->
                            Surface(shape = RoundedCornerShape(14.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${t.pickupArea} ← ${t.dropoffArea}", fontWeight = FontWeight.Bold)
                                        Text("صافي: ${t.driverNetEgp.roundToInt()} ج.م", color = AnqeznyEmerald, fontWeight = FontWeight.Bold)
                                    }
                                    Text("${t.distanceKm} كم • ${t.paymentMethod} • إجمالي: ${t.fareEgp.roundToInt()} ج.م", style = MaterialTheme.typography.bodyMedium, color = AnqeznyInkSecondary)
                                    if (t.rated && t.rating != null) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Star, null, tint = AnqeznyStarGold, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("تقييم العميل: ${t.rating}/5 ${t.reviewComment?.let { "• $it" } ?: ""}", style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    DriverWalletSection(
                        driver = driver,
                        pricingConfig = pricingConfig,
                        transactions = walletTxs,
                        isBusy = isBusy,
                        onRequestTx = { type, amount, method, ref ->
                            onRequestWalletTx(driver, type, amount, method, ref)
                        },
                        onOpenTerms = { showTerms = true },
                        onShowMessage = onShowMessage
                    )
                }

                3 -> {
                    val myReviews = (myReviewsState as? UiState.Success)?.data.orEmpty()
                        .sortedByDescending { it.createdAt?.seconds ?: 0L }
                    DriverPrivateProfileSection(
                        driver = driver,
                        privateDocs = driverPrivateDocs,
                        reviews = myReviews,
                        onGoToWallet = { selectedTab = 2 },
                        onOpenTerms = { showTerms = true },
                        onSwitchToCustomer = onSwitchToCustomer
                    )
                }
            }
        }
    }
}

@Composable
private fun PendingTripOfferCard(trip: Trip, isBusy: Boolean, onAccept: () -> Unit) {
    val isCash = !trip.gatewayLive || trip.paymentMethod == "كاش"
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.5.dp, AnqeznyCrimson),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Surface(shape = RoundedCornerShape(8.dp), color = AnqeznyCrimsonContainer) {
                    Text(
                        text = "طلب إنقاذ جديد • ${trip.distanceKm} كم",
                        color = AnqeznyCrimson,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = "صافي ربحك: ${trip.driverNetEgp.roundToInt()} ج.م",
                    color = AnqeznyEmerald,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Text("استلام من: ${trip.pickupArea}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("تسليم إلى: ${trip.dropoffArea}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = AnqeznyCrimson)
            Text("الفئة: ${trip.vehicleType} • الخدمة: ${trip.serviceType}", style = MaterialTheme.typography.bodyMedium, color = AnqeznyInkSecondary)

            Surface(shape = RoundedCornerShape(10.dp), color = AnqeznySurfaceVariant, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isCash) {
                        "طريقة التحصيل: نقداً من العميل (${trip.fareEgp.roundToInt()} ج.م) — وتُخصم عمولة المنصة (${trip.commissionEgp.roundToInt()} ج.م) من محفظة الضمان."
                    } else {
                        "مدفوع إلكترونياً للمنصة (${trip.paymentMethod}) — يُضاف صافي أرباحك (${trip.driverNetEgp.roundToInt()} ج.م) لرصيد السحب بعد الإنهاء."
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = AnqeznyInk,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Button(
                onClick = onAccept,
                enabled = !isBusy,
                colors = ButtonDefaults.buttonColors(containerColor = AnqeznyCrimson),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("accept_trip_button_${trip.id}")
            ) {
                Text("قبول المهمة الآن", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActiveDriverMissionCard(
    trip: Trip,
    driver: DriverProfile,
    pricingConfig: AppPricingConfig,
    messages: List<TripMessage>,
    isBusy: Boolean,
    onMarkArrived: () -> Unit,
    onSavePhotosAndPin: (List<String>, Boolean) -> Unit,
    onCompleteTrip: (Int) -> Unit,
    onCancel: () -> Unit,
    onSendMessage: (String) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var elapsedWaitSeconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(trip.status, trip.waitingStartedAt) {
        if (trip.status == "arrived" || trip.status == "loaded") {
            val startSec = trip.waitingStartedAt?.seconds ?: (System.currentTimeMillis() / 1000L)
            while (true) {
                val nowSec = System.currentTimeMillis() / 1000L
                elapsedWaitSeconds = (nowSec - startSec).toInt().coerceAtLeast(0)
                delay(1000L)
            }
        }
    }

    val capturedPhotos = remember(trip.id) {
        mutableStateListOf<String>().apply {
            listOfNotNull(trip.photo1Thumb, trip.photo2Thumb, trip.photo3Thumb, trip.photo4Thumb)
                .filter { it.isNotBlank() }
                .forEach { add(it) }
        }
    }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingCameraUri
        if (success && uri != null) {
            scope.launch {
                val thumb = encodeUriToBase64Thumb(context, uri, 300)
                if (thumb != null && capturedPhotos.size < 4) {
                    capturedPhotos.add(thumb)
                }
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createTempCameraUri(context, "car_check")
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            onShowMessage("يمكنك اختيار الصور من المعرض إذا رفضت إذن الكاميرا")
        }
    }

    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val thumb = encodeUriToBase64Thumb(context, uri, 300)
                if (thumb != null && capturedPhotos.size < 4) {
                    capturedPhotos.add(thumb)
                }
            }
        }
    }

    var pinInput by remember { mutableStateOf("") }
    var signaturePoints by remember { mutableIntStateOf(0) }
    var chatInput by remember { mutableStateOf("") }

    Surface(shape = RoundedCornerShape(18.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("العميل: ${trip.customerName}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Surface(shape = RoundedCornerShape(8.dp), color = AnqeznyEmeraldContainer) {
                    Text(
                        text = tripStatusAr(trip.status),
                        color = AnqeznyEmerald,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            GizaRouteMap(
                pickupLat = trip.pickupLat,
                pickupLng = trip.pickupLng,
                pickupLabel = trip.pickupArea,
                dropoffLat = trip.dropoffLat,
                dropoffLng = trip.dropoffLng,
                dropoffLabel = trip.dropoffArea,
                distanceKm = trip.distanceKm,
                tripStage = trip.status
            )

            // Step 1: Mark Arrived
            if (trip.status == "accepted") {
                Button(
                    onClick = onMarkArrived,
                    enabled = !isBusy,
                    colors = ButtonDefaults.buttonColors(containerColor = AnqeznyCharcoal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("driver_arrived_button")
                ) {
                    Text("1. وصلت لموقع العميل (بدء عداد الانتظار)")
                }
            }

            // Waiting timer banner
            if (trip.status == "arrived" || trip.status == "loaded") {
                val mins = elapsedWaitSeconds / 60
                val secs = elapsedWaitSeconds % 60
                Surface(shape = RoundedCornerShape(10.dp), color = AnqeznyAmberContainer, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "عداد الانتظار والتوثيق: ${String.format(Locale.US, "%02d:%02d", mins, secs)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // Step 2 & 3: 4 Photos + Security PIN
            if (trip.status == "arrived") {
                Text(
                    text = "2. التقاط 4 صور للسيارة قبل التحميل (${capturedPhotos.size}/4)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = {
                            if (capturedPhotos.size >= 4) return@OutlinedButton
                            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                            if (granted) {
                                val uri = createTempCameraUri(context, "car_check")
                                pendingCameraUri = uri
                                cameraLauncher.launch(uri)
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        enabled = capturedPhotos.size < 4,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("كاميرا")
                    }
                    OutlinedButton(
                        onClick = {
                            if (capturedPhotos.size < 4) {
                                galleryPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        },
                        enabled = capturedPhotos.size < 4,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("من المعرض")
                    }
                }

                if (capturedPhotos.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        capturedPhotos.forEachIndexed { i, b64 ->
                            val bmp = remember(b64) { decodeBase64Bitmap(b64) }
                            if (bmp != null) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "صورة فحص ${i + 1}",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()

                Text("3. تأكيد كود الأمان (PIN) من العميل", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { pinInput = it },
                    label = { Text("أدخل كود الأمان المكون من 4 أرقام") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("driver_pin_input")
                )

                Button(
                    onClick = {
                        if (pinInput.trim() == trip.pin.trim()) {
                            onSavePhotosAndPin(capturedPhotos.toList(), true)
                        } else {
                            onShowMessage("كود الأمان غير مطابق لكود العميل")
                        }
                    },
                    enabled = !isBusy,
                    colors = ButtonDefaults.buttonColors(containerColor = AnqeznyCharcoal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("verify_pin_button")
                ) {
                    Text("تأكيد الكود والصور وبدء التحرك للوجهة")
                }

                TextButton(
                    onClick = { onSavePhotosAndPin(capturedPhotos.toList(), true) },
                    enabled = !isBusy,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .testTag("manual_pin_override_button")
                ) {
                    Text("تأكيد يدوي (للتجربة)")
                }
            }

            // Step 4 & 5: Loaded -> Head to Dropoff + Signature + Complete
            if (trip.status == "loaded") {
                Surface(shape = RoundedCornerShape(12.dp), color = AnqeznyCrimsonContainer, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "المرحلة الحالية: التحرك نحو وجهة النقل (${trip.dropoffArea}). عند الوصول، اطلب من العميل التوقيع بالأسفل.",
                        color = AnqeznyCrimson,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                SignaturePad(onSignaturePointsChanged = { signaturePoints = it })

                Button(
                    onClick = {
                        if (signaturePoints < 3) {
                            onShowMessage("يرجى توقيع العميل الإلكتروني داخل المربع أولاً")
                        } else {
                            onCompleteTrip(signaturePoints)
                        }
                    },
                    enabled = !isBusy,
                    colors = ButtonDefaults.buttonColors(containerColor = AnqeznyCrimson),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("complete_trip_button")
                ) {
                    Icon(Icons.Default.CheckCircle, null)
                    Spacer(Modifier.width(8.dp))
                    Text("تأكيد إنهاء المهمة واستلام المبلغ والعودة للرئيسية")
                }
            }

            // Messages with customer
            HorizontalDivider()
            if (messages.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    messages.sortedBy { it.createdAt?.seconds ?: 0L }.forEach { m ->
                        Text("${m.senderName}: ${m.text}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = chatInput,
                    onValueChange = { chatInput = it },
                    placeholder = { Text("رسالة للعميل...") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    if (chatInput.isNotBlank()) {
                        onSendMessage(chatInput)
                        chatInput = ""
                    }
                }) {
                    Icon(Icons.Default.Send, contentDescription = "إرسال", tint = AnqeznyCrimson)
                }
            }

            if (trip.status in listOf("accepted", "arrived")) {
                TextButton(onClick = onCancel, modifier = Modifier.align(Alignment.End)) {
                    Text("إلغاء المهمة لظرف طارئ", color = AnqeznyCrimson)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DriverWalletSection(
    driver: DriverProfile,
    pricingConfig: AppPricingConfig,
    transactions: List<WalletTransaction>,
    isBusy: Boolean,
    onRequestTx: (type: String, amount: Double, method: String, ref: String) -> Unit,
    onOpenTerms: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    var topupAmount by remember { mutableStateOf("250") }
    var topupMethod by remember { mutableStateOf("vodafone_cash") }
    var topupRef by remember { mutableStateOf("") }

    var withdrawAmount by remember { mutableStateOf("100") }
    var withdrawMethod by remember { mutableStateOf("vodafone_cash") }
    var withdrawAccount by remember { mutableStateOf("") }

    val progress = (driver.walletBalance / pricingConfig.minWalletEgp.coerceAtLeast(1.0)).toFloat().coerceIn(0f, 1f)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Escrow Wallet Card
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalanceWallet, null, tint = AnqeznyCrimson)
                        Spacer(Modifier.width(6.dp))
                        Text("محفظة الضمان (للعمولات الكاش)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Text("${driver.walletBalance.roundToInt()} ج.م", style = MaterialTheme.typography.headlineSmall, color = AnqeznyCrimson, fontWeight = FontWeight.Bold)
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = if (driver.walletBalance >= pricingConfig.minWalletEgp) AnqeznyEmerald else AnqeznyCrimson
                )
                Text(
                    "الحد الأدنى لاستقبال الطلبات: ${pricingConfig.minWalletEgp.roundToInt()} ج.م (تُخصم منه عمولة المنصة ${pricingConfig.commissionPercent.roundToInt()}% في الرحلات النقدية)",
                    style = MaterialTheme.typography.labelMedium,
                    color = AnqeznyInkSecondary
                )

                HorizontalDivider()
                Text("طلب شحن محفظة الضمان", fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("vodafone_cash" to "فودافون كاش", "instapay" to "إنستا باي", "bank" to "تحويل بنكي").forEach { (k, label) ->
                        FilterChip(selected = topupMethod == k, onClick = { topupMethod = k }, label = { Text(label) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = topupAmount,
                        onValueChange = { topupAmount = it },
                        label = { Text("المبلغ (ج.م)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = topupRef,
                        onValueChange = { topupRef = it },
                        label = { Text("رقم العملية / المرجع") },
                        modifier = Modifier.weight(1.4f)
                    )
                }
                Button(
                    onClick = {
                        val amt = topupAmount.toDoubleOrNull() ?: 0.0
                        if (amt < 50) onShowMessage("الحد الأدنى للشحن 50 ج.م")
                        else if (topupRef.trim().length < 3) onShowMessage("أدخل رقم المرجع")
                        else {
                            onRequestTx("topup", amt, topupMethod, topupRef)
                            topupRef = ""
                        }
                    },
                    enabled = !isBusy,
                    colors = ButtonDefaults.buttonColors(containerColor = AnqeznyCharcoal),
                    modifier = Modifier.fillMaxWidth().testTag("submit_topup_button")
                ) {
                    Text("إرسال طلب شحن الرصيد")
                }
            }
        }

        // Withdrawable Earnings Card
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("أرباح قابلة للسحب (الإلكتروني)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${driver.withdrawableBalance.roundToInt()} ج.م", style = MaterialTheme.typography.headlineSmall, color = AnqeznyEmerald, fontWeight = FontWeight.Bold)
                }
                Text(
                    "الحد الأدنى للسحب ${pricingConfig.minWithdrawEgp.roundToInt()} ج.م بدون أي رسوم سحب.",
                    style = MaterialTheme.typography.labelMedium,
                    color = AnqeznyInkSecondary
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("vodafone_cash" to "فودافون كاش", "instapay" to "إنستا باي", "bank" to "تحويل بنكي").forEach { (k, label) ->
                        FilterChip(selected = withdrawMethod == k, onClick = { withdrawMethod = k }, label = { Text(label) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = withdrawAmount,
                        onValueChange = { withdrawAmount = it },
                        label = { Text("المبلغ للسحب") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = withdrawAccount,
                        onValueChange = { withdrawAccount = it },
                        label = { Text("رقم المحفظة / الحساب") },
                        modifier = Modifier.weight(1.4f)
                    )
                }
                Button(
                    onClick = {
                        val amt = withdrawAmount.toDoubleOrNull() ?: 0.0
                        when {
                            amt < pricingConfig.minWithdrawEgp -> onShowMessage("الحد الأدنى للسحب ${pricingConfig.minWithdrawEgp.roundToInt()} ج.م")
                            amt > driver.withdrawableBalance -> onShowMessage("الرصيد المتاح للسحب غير كافٍ")
                            withdrawAccount.trim().length < 4 -> onShowMessage("أدخل رقم المحفظة أو الحساب البنكي")
                            else -> {
                                onRequestTx("withdrawal", amt, withdrawMethod, withdrawAccount)
                                withdrawAccount = ""
                            }
                        }
                    },
                    enabled = !isBusy,
                    colors = ButtonDefaults.buttonColors(containerColor = AnqeznyEmerald),
                    modifier = Modifier.fillMaxWidth().testTag("submit_withdraw_button")
                ) {
                    Text("طلب سحب الأرباح")
                }
            }
        }

        // Wallet Transaction History
        if (transactions.isNotEmpty()) {
            Text("سجل عمليات الشحن والسحب", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            transactions.forEach { tx ->
                Surface(shape = RoundedCornerShape(12.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(if (tx.type == "topup") "شحن رصيد" else "سحب أرباح", fontWeight = FontWeight.Bold)
                            Text("${tx.method} • ${tx.accountOrRef}", style = MaterialTheme.typography.labelMedium, color = AnqeznyInkSecondary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${tx.amountEgp.roundToInt()} ج.م", fontWeight = FontWeight.Bold)
                            Text(
                                text = when (tx.status) {
                                    "approved" -> "مؤكد"
                                    "rejected" -> "مرفوض"
                                    else -> "قيد المراجعة"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = when (tx.status) {
                                    "approved" -> AnqeznyEmerald
                                    "rejected" -> AnqeznyCrimson
                                    else -> AnqeznyAmber
                                }
                            )
                        }
                    }
                }
            }
        }

        TextButton(onClick = onOpenTerms, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("عرض الشروط والأحكام وسياسة العمولة")
        }
    }
}

@Composable
private fun StatCol(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, color = Color(0xFF94A3B8), style = MaterialTheme.typography.labelMedium)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DriverPrivateProfileSection(
    driver: DriverProfile,
    privateDocs: DriverPrivateDocs?,
    reviews: List<DriverReview>,
    onGoToWallet: () -> Unit,
    onOpenTerms: () -> Unit,
    onSwitchToCustomer: () -> Unit
) {
    val maskedNationalId = privateDocs?.nationalId?.let { id ->
        if (id.length > 4) "••••••••${id.takeLast(4)}" else id
    } ?: "موثق لدى الإدارة"

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Identity Card
        Surface(shape = RoundedCornerShape(18.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, null, tint = AnqeznyEmerald)
                        Spacer(Modifier.width(6.dp))
                        Text(driver.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = AnqeznyEmeraldContainer) {
                        Text(
                            text = "سائق معتمد",
                            color = AnqeznyEmerald,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                HorizontalDivider()

                ProfileDetailRow(Icons.Default.Phone, "رقم الموبايل المسجل", privateDocs?.phone ?: "مسجل بالحساب")
                ProfileDetailRow(Icons.Default.Badge, "الرقم القومي", maskedNationalId)
                ProfileDetailRow(Icons.Default.Badge, "رقم رخصة القيادة", "${privateDocs?.licenseNumber ?: "-"} (تنتهي: ${privateDocs?.licenseExpiry ?: "-"})")
                ProfileDetailRow(Icons.Default.DirectionsCar, "الونش واللوحة", "${driver.winchType} • لوحة: ${driver.plateNumber}")
                ProfileDetailRow(Icons.Default.CheckCircle, "نطاق العمل", "${driver.area} — محافظة الجيزة")
                if (!driver.companyName.isNullOrBlank()) {
                    ProfileDetailRow(Icons.Default.CheckCircle, "تابع لشركة", driver.companyName)
                }
            }
        }

        // Wallet & Performance Snapshot Card
        Surface(shape = RoundedCornerShape(18.dp), color = AnqeznyCharcoal, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("ملخص محفظتك وأرباحك", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatCol("محفظة الضمان", "${driver.walletBalance.roundToInt()} ج.م")
                    StatCol("قابل للسحب", "${driver.withdrawableBalance.roundToInt()} ج.م")
                    StatCol("إجمالي الرحلات", "${driver.totalTrips}")
                }
                Button(
                    onClick = onGoToWallet,
                    colors = ButtonDefaults.buttonColors(containerColor = AnqeznyCrimson),
                    modifier = Modifier.fillMaxWidth().testTag("profile_go_to_wallet_button")
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("إدارة الشحن وسحب الأرباح")
                }
            }
        }

        // Customer Rating & Reviews Card
        Surface(shape = RoundedCornerShape(18.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("تقييمات العملاء عنك", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = AnqeznyStarGold, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (driver.ratingCount > 0) String.format(Locale.US, "%.1f / 5 (%d)", driver.ratingAvg, driver.ratingCount) else "لا توجد تقييمات بعد",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (reviews.isEmpty()) {
                    Text("ستظهر هنا تعليقات العملاء ونجومهم بعد إتمام الرحلات.", style = MaterialTheme.typography.bodyMedium, color = AnqeznyInkSecondary)
                } else {
                    reviews.take(10).forEach { rev ->
                        Surface(shape = RoundedCornerShape(12.dp), color = AnqeznySurfaceVariant, modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(rev.customerName, fontWeight = FontWeight.Bold)
                                    Text("★ ${rev.stars}/5", color = AnqeznyStarGold, fontWeight = FontWeight.Bold)
                                }
                                if (rev.tags.isNotEmpty()) {
                                    Text(rev.tags.joinToString(" • "), style = MaterialTheme.typography.labelMedium, color = AnqeznyCrimson)
                                }
                                if (rev.comment.isNotBlank()) {
                                    Text(rev.comment, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Utility Actions
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onOpenTerms, modifier = Modifier.weight(1f)) {
                Text("الشروط والعمولة")
            }
            OutlinedButton(onClick = onSwitchToCustomer, modifier = Modifier.weight(1f)) {
                Text("اطلب ونش كعميل")
            }
        }
    }
}

@Composable
private fun ProfileDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(icon, contentDescription = null, tint = AnqeznyCrimson, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = AnqeznyInkSecondary)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = AnqeznyInk, fontWeight = FontWeight.Bold)
        }
    }
}
