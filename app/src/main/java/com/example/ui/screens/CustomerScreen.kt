package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.model.AppPricingConfig
import com.example.data.model.DriverProfile
import com.example.data.model.DriverReview
import com.example.data.model.GIZA_AREAS
import com.example.data.model.PaymentMethod
import com.example.data.model.ServiceType
import com.example.data.model.Trip
import com.example.data.model.TripMessage
import com.example.data.model.UserProfile
import com.example.data.model.VehicleType
import com.example.data.model.calculateDistanceKm
import com.example.data.model.calculateTripPricing
import com.example.ui.components.DriverProfileSheet
import com.example.ui.components.GizaRouteMap
import com.example.ui.components.MapSelectionMode
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
import com.example.util.fetchCurrentGpsLocation
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CustomerScreen(
    userProfile: UserProfile?,
    pricingConfig: AppPricingConfig,
    tripsState: UiState<List<Trip>>,
    approvedDriversState: UiState<List<DriverProfile>>,
    tripMessagesState: UiState<List<TripMessage>>,
    driverReviewsState: UiState<List<DriverReview>>,
    isBusy: Boolean,
    onWatchTripMessages: (String?) -> Unit,
    onWatchDriverReviews: (String?) -> Unit,
    onCreateTrip: (
        customerName: String, customerPhone: String,
        pickupArea: String, pickupLat: Double, pickupLng: Double,
        dropoffArea: String, dropoffLat: Double, dropoffLng: Double,
        distanceKm: Double, vehicleType: String, serviceType: String, scheduledAt: String?,
        paymentMethod: String, gatewayLive: Boolean, fareEgp: Double, commissionEgp: Double, driverNetEgp: Double
    ) -> Unit,
    onCancelTrip: (Trip, Double, String) -> Unit,
    onSendMessage: (Trip, String, String, String) -> Unit,
    onSubmitRating: (Trip, Int, String, List<String>) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showTerms by remember { mutableStateOf(false) }
    var inspectedDriver by remember { mutableStateOf<DriverProfile?>(null) }

    // Booking form state
    var pickupAreaName by remember { mutableStateOf(GIZA_AREAS[1].nameAr) } // المهندسين
    var pickupLat by remember { mutableStateOf(GIZA_AREAS[1].lat) }
    var pickupLng by remember { mutableStateOf(GIZA_AREAS[1].lng) }
    var pickupExpanded by remember { mutableStateOf(false) }

    var dropoffAreaName by remember { mutableStateOf(GIZA_AREAS[9].nameAr) } // الشيخ زايد
    var dropoffLat by remember { mutableStateOf(GIZA_AREAS[9].lat) }
    var dropoffLng by remember { mutableStateOf(GIZA_AREAS[9].lng) }
    var dropoffExpanded by remember { mutableStateOf(false) }

    var mapMode by remember { mutableStateOf(MapSelectionMode.NONE) }
    var selectedVehicle by remember { mutableStateOf(VehicleType.SEDAN) }
    var selectedService by remember { mutableStateOf(ServiceType.INSTANT) }
    var scheduledAt by remember { mutableStateOf("اليوم - 06:00 مساءً") }
    var selectedPayment by remember { mutableStateOf(PaymentMethod.CASH) }
    var contactPhone by remember(userProfile?.phone) { mutableStateOf(userProfile?.phone ?: "") }

    val distanceKm = remember(pickupLat, pickupLng, dropoffLat, dropoffLng) {
        calculateDistanceKm(pickupLat, pickupLng, dropoffLat, dropoffLng)
    }
    val pricing = remember(distanceKm, selectedVehicle, selectedService, pricingConfig) {
        calculateTripPricing(distanceKm, selectedVehicle, selectedService, pricingConfig)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scope.launch {
                val loc = fetchCurrentGpsLocation(context)
                if (loc != null) {
                    pickupLat = loc.first.coerceIn(29.90, 30.15)
                    pickupLng = loc.second.coerceIn(30.90, 31.25)
                    val nearest = GIZA_AREAS.minByOrNull { a ->
                        val dl = a.lat - pickupLat
                        val dg = a.lng - pickupLng
                        dl * dl + dg * dg
                    }
                    pickupAreaName = "موقعي (${nearest?.nameAr ?: "الجيزة"})"
                    onShowMessage("تم تحديد موقعك الحالي على الخريطة")
                } else {
                    onShowMessage("تعذر قراءة GPS الآن — يمكنك التحديد من الخريطة")
                }
            }
        } else {
            onShowMessage("تم رفض إذن الموقع — يمكنك اختيار المنطقة أو النقر على الخريطة")
        }
    }

    val allTrips = (tripsState as? UiState.Success)?.data.orEmpty()
        .sortedByDescending { it.createdAt?.seconds ?: 0L }
    val activeTrip = allTrips.firstOrNull {
        it.status in listOf("pending", "accepted", "arrived", "loaded") || (it.status == "completed" && !it.rated)
    }
    val approvedDrivers = (approvedDriversState as? UiState.Success)?.data.orEmpty()

    LaunchedEffect(activeTrip?.id) {
        onWatchTripMessages(activeTrip?.id)
    }

    if (showTerms) {
        TermsDialog(onDismiss = { showTerms = false })
    }

    inspectedDriver?.let { driver ->
        DriverProfileSheet(
            driver = driver,
            reviewsState = driverReviewsState,
            onDismiss = {
                inspectedDriver = null
                onWatchDriverReviews(null)
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AnqeznyBackground),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = AnqeznyCrimson
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(if (activeTrip != null) "تتبع الطلب الحالي" else "طلب ونش جديد") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("السائقون والرحلات (${approvedDrivers.size})") }
            )
        }

        Column(
            modifier = Modifier
                .widthIn(max = 620.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (selectedTab == 0) {
                if (activeTrip != null) {
                    ActiveTripCustomerCard(
                        trip = activeTrip,
                        pricingConfig = pricingConfig,
                        approvedDrivers = approvedDrivers,
                        messages = (tripMessagesState as? UiState.Success)?.data.orEmpty(),
                        isBusy = isBusy,
                        onInspectDriver = { driver ->
                            inspectedDriver = driver
                            onWatchDriverReviews(driver.userId)
                        },
                        onDial = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:01000000000"))
                            context.startActivity(intent)
                        },
                        onSendMessage = { msg ->
                            onSendMessage(activeTrip, userProfile?.displayName ?: "العميل", "customer", msg)
                        },
                        onCancel = { reason ->
                            onCancelTrip(activeTrip, pricingConfig.cancelFeeEgp, reason)
                        },
                        onSubmitRating = { stars, comment, tags ->
                            onSubmitRating(activeTrip, stars, comment, tags)
                        }
                    )
                } else {
                    // Giza Coverage Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AnqeznyCharcoal,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.LocationOn, null, tint = AnqeznyCrimson)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "نطاق الخدمة الحالي: محافظة الجيزة (14 منطقة + التحديد الحر)",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                            TextButton(onClick = { showTerms = true }) {
                                Text("الشروط", color = Color(0xFFFCA5A5))
                            }
                        }
                    }

                    // Pickup & Dropoff Card
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "تحديد المسار (من -> إلى)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            // Pickup selector
                            ExposedDropdownMenuBox(
                                expanded = pickupExpanded,
                                onExpandedChange = { pickupExpanded = !pickupExpanded }
                            ) {
                                OutlinedTextField(
                                    value = pickupAreaName,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("موقع العطل / استلام السيارة (Pickup)") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pickupExpanded) },
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = pickupExpanded,
                                    onDismissRequest = { pickupExpanded = false }
                                ) {
                                    GIZA_AREAS.forEach { area ->
                                        DropdownMenuItem(
                                            text = { Text(area.nameAr) },
                                            onClick = {
                                                pickupAreaName = area.nameAr
                                                pickupLat = area.lat
                                                pickupLng = area.lng
                                                pickupExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                                                       Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = {
                                        val granted = ContextCompat.checkSelfPermission(
                                            context, Manifest.permission.ACCESS_FINE_LOCATION
                                        ) == PackageManager.PERMISSION_GRANTED ||
                                            ContextCompat.checkSelfPermission(
                                                context, Manifest.permission.ACCESS_COARSE_LOCATION
                                            ) == PackageManager.PERMISSION_GRANTED
                                        if (granted) {
                                            scope.launch {
                                                val loc = fetchCurrentGpsLocation(context)
                                                if (loc != null) {
                                                    pickupLat = loc.first
                                                    pickupLng = loc.second
                                                    pickupAreaName = "موقعي الحالي (GPS)"
                                                    onShowMessage("تم تحديد موقعك الحالي بنجاح")
                                                } else {
                                                    onShowMessage("تعذر جلب إشارة GPS — تأكد من تشغيل الموقع أو المس الخريطة مباشرة")
                                                }
                                            }
                                        } else {
                                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("my_location_button")
                                ) {
                                    Icon(Icons.Default.GpsFixed, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("موقعي الحالي")
                                }

                                OutlinedButton(
                                    onClick = {
                                        mapMode = if (mapMode == MapSelectionMode.PICKUP) MapSelectionMode.NONE else MapSelectionMode.PICKUP
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        if (mapMode == MapSelectionMode.PICKUP) AnqeznyCharcoal else MaterialTheme.colorScheme.outline
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Map, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (mapMode == MapSelectionMode.PICKUP) "جاري التحديد..." else "حدد الاستلام")
                                }
                            }

                            // Dropoff selector
                            ExposedDropdownMenuBox(
                                expanded = dropoffExpanded,
                                onExpandedChange = { dropoffExpanded = !dropoffExpanded }
                            ) {
                                OutlinedTextField(
                                    value = dropoffAreaName,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("وجهة النقل / تسليم السيارة (Drop-off)") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropoffExpanded) },
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = dropoffExpanded,
                                    onDismissRequest = { dropoffExpanded = false }
                                ) {
                                    GIZA_AREAS.forEach { area ->
                                        DropdownMenuItem(
                                            text = { Text(area.nameAr) },
                                            onClick = {
                                                dropoffAreaName = area.nameAr
                                                dropoffLat = area.lat
                                                dropoffLng = area.lng
                                                dropoffExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    mapMode = if (mapMode == MapSelectionMode.DROPOFF) MapSelectionMode.NONE else MapSelectionMode.DROPOFF
                                },
                                border = BorderStroke(
                                    1.dp,
                                    if (mapMode == MapSelectionMode.DROPOFF) AnqeznyCrimson else MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Map, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(if (mapMode == MapSelectionMode.DROPOFF) "انقر على الخريطة لتحديد الوجهة" else "حدد وجهة النقل على الخريطة")
                            }

                            GizaRouteMap(
                                pickupLat = pickupLat,
                                pickupLng = pickupLng,
                                pickupLabel = pickupAreaName,
                                dropoffLat = dropoffLat,
                                dropoffLng = dropoffLng,
                                dropoffLabel = dropoffAreaName,
                                distanceKm = distanceKm,
                                selectionMode = mapMode,
                                onMapTap = { lat, lng, label ->
                                    if (mapMode == MapSelectionMode.DROPOFF) {
                                        dropoffLat = lat
                                        dropoffLng = lng
                                        dropoffAreaName = label
                                        onShowMessage("تم تحديد وجهة النقل: $label")
                                    } else {
                                        pickupLat = lat
                                        pickupLng = lng
                                        pickupAreaName = label
                                        onShowMessage("تم تحديد موقع الاستلام: $label")
                                    }
                                    mapMode = MapSelectionMode.NONE
                                }
                            )
                        }
                    }


                            if (selectedService == ServiceType.SCHEDULED) {
                                OutlinedTextField(
                                    value = scheduledAt,
                                    onValueChange = { scheduledAt = it },
                                    label = { Text("تاريخ وساعة النقل المحددة") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Text("طريقة الدفع", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                PaymentMethod.entries.forEach { pm ->
                                    FilterChip(
                                        selected = selectedPayment == pm,
                                        onClick = { selectedPayment = pm },
                                        label = { Text(pm.labelAr) }
                                    )
                                }
                            }

                            if (selectedPayment.electronic && !pricingConfig.gatewayLive) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = AnqeznyAmberContainer,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "تنبيه: بوابة الدفع الإلكتروني (${selectedPayment.labelAr}) قيد التفعيل النهائي؛ يُسجل اختيارك حالياً ويتم التحصيل نقداً من السائق لحين التفعيل الفعلي.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AnqeznyInk,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Locked Price Breakdown Card
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White,
                        border = BorderStroke(1.5.dp, AnqeznyCrimson.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("تفاصيل السعر المقفول", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            PriceRow("فتح العداد الأساسي", "${pricing.baseFareEgp.roundToInt()} ج.م")
                            PriceRow("تكلفة المسافة (${pricing.distanceKm} كم)", "${pricing.distanceCostEgp.roundToInt()} ج.م")
                            PriceRow("مؤشر السولار (${pricingConfig.dieselPriceEgp} ج.م/لتر)", "${pricing.fuelFactorEgp.roundToInt()} ج.م")
                            PriceRow("إضافات الفئة والخدمة", "${pricing.vehicleAndServiceExtraEgp.roundToInt()} ج.م")
                            PriceRow("ضريبة القيمة المضافة (14%)", "${pricing.vatEgp.roundToInt()} ج.م")
                            HorizontalDivider()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("الإجمالي المقفول", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "${pricing.totalFareEgp.roundToInt()} ج.م",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = AnqeznyCrimson,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(
                                value = contactPhone,
                                onValueChange = { contactPhone = it },
                                label = { Text("رقم موبايل التواصل عند وصول الونش") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 6.dp)
                                    .testTag("customer_booking_phone_input")
                            )
                            Button(
                                onClick = {
                                    val safePhone = contactPhone.trim().ifBlank { userProfile?.phone ?: "" }
                                    if (safePhone.length < 10) {
                                        onShowMessage("يرجى إدخال رقم موبايل صحيح للتواصل معك")
                                        return@Button
                                    }
                                    onCreateTrip(
                                        userProfile?.displayName?.ifBlank { "عميل" } ?: "عميل",
                                        safePhone,
                                        pickupAreaName,
                                        pickupLat,
                                        pickupLng,
                                        dropoffAreaName,
                                        dropoffLat,
                                        dropoffLng,
                                        distanceKm,
                                        selectedVehicle.labelAr,
                                        selectedService.labelAr,
                                        if (selectedService == ServiceType.SCHEDULED) scheduledAt else null,
                                        selectedPayment.labelAr,
                                        pricingConfig.gatewayLive,
                                        pricing.totalFareEgp,
                                        pricing.commissionEgp,
                                        pricing.driverNetEgp
                                    )
                                },
                                enabled = !isBusy,
                                colors = ButtonDefaults.buttonColors(containerColor = AnqeznyCrimson),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("create_trip_button")
                            ) {
                                Icon(Icons.Default.DirectionsCar, null)
                                Spacer(Modifier.width(8.dp))
                                Text("طلب ونش الآن (${pricing.totalFareEgp.roundToInt()} ج.م)", style = MaterialTheme.typography.titleMedium)
                                                      }
                        }
                    }
                } else {

                // Tab 1: Verified Drivers Directory (with public ratings!) & Trip History
                Text(
                    text = "السائقون الموثقون في الجيزة (اضغط لعرض التقييمات)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (approvedDrivers.isEmpty()) {
                    Surface(shape = RoundedCornerShape(14.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "لا يوجد سائقون موثقون بعد — سيتم عرض ملفات السائقين وتقييماتهم فور اعتمادهم.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AnqeznyInkSecondary,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                } else {
                    approvedDrivers.forEach { drv ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    inspectedDriver = drv
                                    onWatchDriverReviews(drv.userId)
                                }
                                .testTag("driver_directory_card_${drv.userId}")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(drv.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.width(4.dp))
                                        Icon(Icons.Default.Verified, null, tint = AnqeznyEmerald, modifier = Modifier.size(16.dp))
                                        if (drv.online) {
                                            Spacer(Modifier.width(6.dp))
                                            Surface(shape = RoundedCornerShape(8.dp), color = AnqeznyEmeraldContainer) {
                                                Text(
                                                    "متاح الآن",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = AnqeznyEmerald,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        "${drv.winchType} • ${drv.area} • لوحة: ${drv.plateNumber}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AnqeznyInkSecondary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, null, tint = AnqeznyStarGold, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(3.dp))
                                        Text(
                                            text = if (drv.ratingCount > 0) String.format(Locale.US, "%.1f", drv.ratingAvg) else "جديد",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "(${drv.ratingCount} تقييم)",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = AnqeznyInkSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))
                Text("سجل رحلاتي", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (allTrips.isEmpty()) {
                    Text("لم تقم بأي رحلات بعد.", style = MaterialTheme.typography.bodyMedium, color = AnqeznyInkSecondary)
                } else {
                    allTrips.forEach { t ->
                        Surface(shape = RoundedCornerShape(14.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${t.pickupArea} ← ${t.dropoffArea}", fontWeight = FontWeight.Bold)
                                    Text("${t.fareEgp.roundToInt()} ج.م", color = AnqeznyCrimson, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    "الحالة: ${tripStatusAr(t.status)} • ${t.vehicleType}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AnqeznyInkSecondary
                                )
                                if (t.rated && t.rating != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, null, tint = AnqeznyStarGold, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("تقييمك: ${t.rating}/5 ${t.reviewComment?.let { "• $it" } ?: ""}", style = MaterialTheme.typography.labelMedium)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActiveTripCustomerCard(
    trip: Trip,
    pricingConfig: AppPricingConfig,
    approvedDrivers: List<DriverProfile>,
    messages: List<TripMessage>,
    isBusy: Boolean,
    onInspectDriver: (DriverProfile) -> Unit,
    onDial: () -> Unit,
    onSendMessage: (String) -> Unit,
    onCancel: (String) -> Unit,
    onSubmitRating: (Int, String, List<String>) -> Unit
) {
    var chatInput by remember { mutableStateOf("") }
    var selectedStars by remember { mutableIntStateOf(5) }
    var reviewComment by remember { mutableStateOf("") }
    val selectedTags = remember { mutableStateListOf<String>() }

    val quickMessages = listOf(
        "أنا في الانتظار عند السيارة",
        "السيارة في وضع النيوترال وجاهزة",
        "هل اقتربت من الموقع؟",
        "يرجى الاتصال بي عند الوصول"
    )
    val reviewTags = listOf("ممتاز في المواعيد", "أمان في التحميل", "تعامل راقي", "التزام بالسعر", "سرعة الاستجابة")

    val assignedDriverObj = approvedDrivers.firstOrNull { it.userId == trip.driverId }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = RoundedCornerShape(10.dp), color = AnqeznyCrimsonContainer) {
                        Text(
                            text = tripStatusAr(trip.status),
                            color = AnqeznyCrimson,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    Text(
                        text = "${trip.fareEgp.roundToInt()} ج.م (${trip.paymentMethod})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AnqeznyInk
                    )
                }

                // Security PIN badge
                if (trip.status != "completed") {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AnqeznyCharcoal,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("كود الأمان لبدء التحميل (PIN)", color = Color(0xFFCBD5E1), style = MaterialTheme.typography.labelMedium)
                                Text("أعطِ الكود للسائق بعد التقاط الـ 4 صور", color = Color(0xFF94A3B8), style = MaterialTheme.typography.labelMedium)
                            }
                            Text(
                                text = trip.pin,
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
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

                // Assigned Driver Info
                if (!trip.driverName.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AnqeznySurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val profile = assignedDriverObj ?: DriverProfile(
                                    userId = trip.driverId ?: "",
                                    name = trip.driverName ?: "",
                                    winchType = "ونش إنقاذ",
                                    plateNumber = trip.driverPlate ?: "",
                                    area = trip.pickupArea,
                                    status = "approved"
                                )
                                onInspectDriver(profile)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Person, null, tint = AnqeznyCharcoal)
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(trip.driverName ?: "", fontWeight = FontWeight.Bold)
                                        if (assignedDriverObj != null && assignedDriverObj.ratingCount > 0) {
                                            Spacer(Modifier.width(6.dp))
                                            Icon(Icons.Default.Star, null, tint = AnqeznyStarGold, modifier = Modifier.size(16.dp))
                                            Text(
                                                " ${String.format(Locale.US, "%.1f", assignedDriverObj.ratingAvg)}",
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Text(
                                        "لوحة الونش: ${trip.driverPlate ?: ""} • اضغط لعرض الملف والتقييمات",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = AnqeznyInkSecondary
                                    )
                                }
                            }
                            IconButton(onClick = onDial) {
                                Icon(Icons.Default.Phone, contentDescription = "اتصال بالسائق", tint = AnqeznyEmerald)
                            }
                        }
                    }

                    // Quick Chat
                    Text("رسائل سريعة مع السائق", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickMessages.forEach { qm ->
                            FilterChip(
                                selected = false,
                                onClick = { onSendMessage(qm) },
                                label = { Text(qm, style = MaterialTheme.typography.labelMedium) }
                            )
                        }
                    }

                    if (messages.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            messages.sortedBy { it.createdAt?.seconds ?: 0L }.forEach { msg ->
                                Text(
                                    text = "${msg.senderName}: ${msg.text}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (msg.senderRole == "driver") AnqeznyCrimson else AnqeznyInk
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = chatInput,
                            onValueChange = { chatInput = it },
                            placeholder = { Text("اكتب رسالة للسائق...") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                if (chatInput.isNotBlank()) {
                                    onSendMessage(chatInput)
                                    chatInput = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "إرسال", tint = AnqeznyCrimson)
                        }
                    }
                }

                // Cancel button (when pending/accepted/arrived)
                if (trip.status in listOf("pending", "accepted", "arrived")) {
                    OutlinedButton(
                        onClick = { onCancel("إلغاء من العميل") },
                        enabled = !isBusy,
                        border = BorderStroke(1.dp, AnqeznyCrimson),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cancel_trip_button")
                    ) {
                        Icon(Icons.Default.Cancel, null, tint = AnqeznyCrimson)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (trip.status == "pending") "إلغاء الطلب (مجاني قبل التحرك)"
                            else "إلغاء الطلب (رسوم إلغاء ${pricingConfig.cancelFeeEgp.roundToInt()} ج.م بعد تحرك السائق)",
                            color = AnqeznyCrimson
                        )
                    }
                }
            }
        }

        // Detailed 1-5 Star Rating + Comment Card on completion
        if (trip.status == "completed" && !trip.rated) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                border = BorderStroke(2.dp, AnqeznyStarGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "تم تسليم سيارتك بالسلامة! قيّم السائق",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "تقييمك من 1 إلى 5 نجوم وتعليقك يظهران في الملف العام للسائق أمام جميع العملاء.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AnqeznyInkSecondary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (star in 1..5) {
                            IconButton(
                                onClick = { selectedStars = star },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("rate_star_$star")
                            ) {
                                Icon(
                                    imageVector = if (star <= selectedStars) Icons.Default.Star else Icons.Outlined.StarOutline,
                                    contentDescription = "$star نجوم",
                                    tint = if (star <= selectedStars) AnqeznyStarGold else AnqeznyInkSecondary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        reviewTags.forEach { tag ->
                            val picked = tag in selectedTags
                            FilterChip(
                                selected = picked,
                                onClick = {
                                    if (picked) selectedTags.remove(tag) else selectedTags.add(tag)
                                },
                                label = { Text(tag, style = MaterialTheme.typography.labelMedium) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = reviewComment,
                        onValueChange = { if (it.length <= 250) reviewComment = it },
                        label = { Text("اكتب تعليقاً قصيراً عن تجربتك مع السائق") },
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("review_comment_input")
                    )

                    Button(
                        onClick = { onSubmitRating(selectedStars, reviewComment, selectedTags.toList()) },
                        enabled = !isBusy,
                        colors = ButtonDefaults.buttonColors(containerColor = AnqeznyCrimson),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("submit_rating_button")
                    ) {
                        Icon(Icons.Default.ChatBubbleOutline, null)
                        Spacer(Modifier.width(8.dp))
                        Text("إرسال التقييم وإغلاق الرحلة ($selectedStars ★)")
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = AnqeznyInkSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = AnqeznyInk)
    }
}

fun tripStatusAr(status: String): String = when (status) {
    "pending" -> "جاري البحث عن سائق"
    "accepted" -> "السائق في الطريق إليك"
    "arrived" -> "السائق وصل لموقع الاستلام"
    "loaded" -> "تم التحميل — جاري النقل للوجهة"
    "completed" -> "تم التسليم بنجاح"
    "cancelled" -> "ملغاة"
    else -> status
}
