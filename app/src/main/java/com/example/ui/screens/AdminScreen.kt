package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.AppPricingConfig
import com.example.data.model.CompanyProfile
import com.example.data.model.DriverProfile
import com.example.data.model.GIZA_AREAS
import com.example.data.model.ServiceType
import com.example.data.model.Trip
import com.example.data.model.VehicleType
import com.example.data.model.WalletTransaction
import com.example.data.model.calculateDistanceKm
import com.example.data.model.calculateTripPricing
import com.example.ui.theme.AnqeznyAmber
import com.example.ui.theme.AnqeznyBackground
import com.example.ui.theme.AnqeznyCharcoal
import com.example.ui.theme.AnqeznyCrimson
import com.example.ui.theme.AnqeznyEmerald
import com.example.ui.theme.AnqeznyInkSecondary
import com.example.ui.theme.AnqeznyStarGold
import com.example.ui.viewmodel.UiState
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    pricingConfig: AppPricingConfig,
    driversState: UiState<List<DriverProfile>>,
    companiesState: UiState<List<CompanyProfile>>,
    tripsState: UiState<List<Trip>>,
    walletTxState: UiState<List<WalletTransaction>>,
    isBusy: Boolean,
    onDispatchPhoneTrip: (
        customerName: String, customerPhone: String,
        pickupArea: String, pickupLat: Double, pickupLng: Double,
        dropoffArea: String, dropoffLat: Double, dropoffLng: Double,
        distanceKm: Double, vehicleType: String, serviceType: String,
        fareEgp: Double, commissionEgp: Double, driverNetEgp: Double,
        assignedDriver: DriverProfile
    ) -> Unit,
    onUpdateDriverStatus: (String, String, String?) -> Unit,
    onUpdateCompanyStatus: (String, String, String?) -> Unit,
    onProcessWalletTx: (WalletTransaction, Boolean) -> Unit,
    onSavePricing: (AppPricingConfig) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("الموزع والرحلات", "السائقون", "الشركات", "المحافظ", "التسعير والعمولة")

    val drivers = (driversState as? UiState.Success)?.data.orEmpty()
    val companies = (companiesState as? UiState.Success)?.data.orEmpty()
    val trips = (tripsState as? UiState.Success)?.data.orEmpty()
        .sortedByDescending { it.createdAt?.seconds ?: 0L }
    val walletTxs = (walletTxState as? UiState.Success)?.data.orEmpty()
        .sortedByDescending { it.createdAt?.seconds ?: 0L }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AnqeznyBackground),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(color = AnqeznyCharcoal, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("لوحة تحكم الإدارة وموزع الطلبات", color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "السائقون: ${drivers.size} • الشركات: ${companies.size} • الرحلات: ${trips.size}",
                    color = Color(0xFFCBD5E1),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = AnqeznyCrimson,
            edgePadding = 8.dp
        ) {
            tabs.forEachIndexed { idx, label ->
                Tab(selected = selectedTab == idx, onClick = { selectedTab = idx }, text = { Text(label) })
            }
        }

        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    Text("الرحلات الحية والتقييمات", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    if (trips.isEmpty()) {
                        Text("لا توجد رحلات مسجلة حالياً.", color = AnqeznyInkSecondary)
                    } else {
                        trips.forEach { t ->
                            Surface(shape = RoundedCornerShape(14.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${t.customerName} (${t.pickupArea} ← ${t.dropoffArea})", fontWeight = FontWeight.Bold)
                                        Text("${t.fareEgp.roundToInt()} ج.م", color = AnqeznyCrimson, fontWeight = FontWeight.Bold)
                                    }
                                    Text("السائق: ${t.driverName ?: "غير مسند"} • الحالة: ${tripStatusAr(t.status)}", style = MaterialTheme.typography.bodyMedium, color = AnqeznyInkSecondary)
                                    if (t.rated && t.rating != null) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Star, null, tint = AnqeznyStarGold, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("${t.rating}/5 ${t.reviewComment?.let { "• $it" } ?: ""}", style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    if (drivers.isEmpty()) {
                        Text("لا يوجد سائقون مسجلون بعد.", color = AnqeznyInkSecondary)
                    } else {
                        drivers.forEach { drv ->
                            var showDocsDialog by remember(drv.userId) { mutableStateOf(false) }
                            var docsInfo by remember(drv.userId) { mutableStateOf<Map<String, Any>?>(null) }

                            if (showDocsDialog) {
                                androidx.compose.material3.AlertDialog(
                                    onDismissRequest = { showDocsDialog = false },
                                    title = { Text("مستندات السائق: ${drv.name}", fontWeight = FontWeight.Bold) },
                                    text = {
                                        Column(
                                            modifier = Modifier.verticalScroll(rememberScrollState()),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (docsInfo == null) {
                                                Text("جاري تحميل الأوراق والصور...")
                                            } else if (docsInfo!!.isEmpty()) {
                                                Text("لم يقم السائق برفع صور مستندات بعد.", color = AnqeznyCrimson)
                                            } else {
                                                Text("الموبايل: ${docsInfo!!["phone"] ?: "غير مسجل"}")
                                                Text("الرقم القومي: ${docsInfo!!["nationalId"] ?: "غير مسجل"}")
                                                Text("رقم الرخصة: ${docsInfo!!["licenseNumber"] ?: "غير مسجل"} (تنتهي: ${docsInfo!!["licenseExpiry"] ?: "-"})")
                                                val docKeys = listOf(
                                                    "docIdThumb" to "1. صورة البطاقة الشخصية",
                                                    "docDriverLicenseThumb" to "2. صورة رخصة القيادة",
                                                    "docVehicleLicenseThumb" to "3. صورة رخصة الونش",
                                                    "docTruckPhotoThumb" to "4. صورة الونش"
                                                )
                                                docKeys.forEach { (key, title) ->
                                                    val b64 = docsInfo!![key] as? String
                                                    Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                                    if (!b64.isNullOrBlank()) {
                                                        val bmp = remember(b64) {
                                                            try {
                                                                val bytes = android.util.Base64.decode(b64, android.util.Base64.DEFAULT)
                                                                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                                            } catch (_: Exception) { null }
                                                        }
                                                        if (bmp != null) {
                                                            androidx.compose.foundation.Image(
                                                               bitmap = bmp.asImageBitmap(),

                                                                contentDescription = title,
                                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                                            )
                                                        }
                                                    } else {
                                                        Text("— لم تُرفع صورة —", color = AnqeznyInkSecondary, style = MaterialTheme.typography.bodySmall)
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        Button(onClick = { showDocsDialog = false }) { Text("إغلاق") }
                                    }
                                )
                            }

                            Surface(shape = RoundedCornerShape(14.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(drv.name, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = statusAr(drv.status),
                                            color = statusColor(drv.status),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text("${drv.winchType} • لوحة: ${drv.plateNumber} • ${drv.area}", style = MaterialTheme.typography.bodyMedium, color = AnqeznyInkSecondary)
                                    Text("محفظة: ${drv.walletBalance.roundToInt()} ج.م • أرباح: ${drv.withdrawableBalance.roundToInt()} ج.م • تقييم: ${drv.ratingAvg} ★ (${drv.ratingCount})", style = MaterialTheme.typography.labelMedium)

                                    OutlinedButton(
                                        onClick = {
                                            showDocsDialog = true
                                            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                                .collection("drivers").document(drv.userId)
                                                .collection("private").document("docs")
                                                .get()
                                                .addOnSuccessListener { snap ->
                                                    docsInfo = snap.data ?: emptyMap()
                                                }
                                                .addOnFailureListener {
                                                    docsInfo = emptyMap()
                                                }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("مراجعة الأوراق والصور المرفوعة", color = AnqeznyCharcoal)
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (drv.status != "approved") {
                                            Button(
                                                onClick = { onUpdateDriverStatus(drv.userId, "approved", null) },
                                                enabled = !isBusy,
                                                colors = ButtonDefaults.buttonColors(containerColor = AnqeznyEmerald),
                                                modifier = Modifier.weight(1f)
                                            ) { Text("توثيق وقبول") }
                                        }
                                        if (drv.status != "rejected") {
                                            OutlinedButton(

onClick = { onUpdateDriverStatus(drv.userId, "rejected", "يرجى مراجعة وضوح الأوراق") },
enabled = !isBusy,
modifier = Modifier.weight(1f)
) { Text("رفض / إيقاف", color = AnqeznyCrimson) }
}
}
}
}
}
}
}
                2 -> {
                    if (companies.isEmpty()) {
                        Text("لا توجد شركات مسجلة بعد.", color = AnqeznyInkSecondary)
                    } else {
                        companies.forEach { comp ->
                            Surface(shape = RoundedCornerShape(14.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(comp.companyName, fontWeight = FontWeight.Bold)
                                        Text(statusAr(comp.status), color = statusColor(comp.status), fontWeight = FontWeight.Bold)
                                    }
                                    Text("المسؤول: ${comp.managerName} • سجل: ${comp.commercialReg} • ضريبية: ${comp.taxCard}", style = MaterialTheme.typography.bodyMedium, color = AnqeznyInkSecondary)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (comp.status != "approved") {
                                            Button(
                                                onClick = { onUpdateCompanyStatus(comp.userId, "approved", null) },
                                                enabled = !isBusy,
                                                colors = ButtonDefaults.buttonColors(containerColor = AnqeznyEmerald),
                                                modifier = Modifier.weight(1f)
                                            ) { Text("اعتماد الشركة") }
                                        }
                                        if (comp.status != "rejected") {
                                            OutlinedButton(
                                                onClick = { onUpdateCompanyStatus(comp.userId, "rejected", "مستندات غير مكتملة") },
                                                enabled = !isBusy,
                                                modifier = Modifier.weight(1f)
                                            ) { Text("رفض", color = AnqeznyCrimson) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    if (walletTxs.isEmpty()) {
                        Text("لا توجد طلبات شحن أو سحب حالياً.", color = AnqeznyInkSecondary)
                    } else {
                        walletTxs.forEach { tx ->
                            Surface(shape = RoundedCornerShape(14.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${tx.driverName} (${if (tx.type == "topup") "شحن" else "سحب"})", fontWeight = FontWeight.Bold)
                                        Text("${tx.amountEgp.roundToInt()} ج.م", color = AnqeznyCrimson, fontWeight = FontWeight.Bold)
                                    }
                                    Text("${tx.method} • مرجع/حساب: ${tx.accountOrRef}", style = MaterialTheme.typography.bodyMedium, color = AnqeznyInkSecondary)
                                    if (tx.status == "pending") {
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = { onProcessWalletTx(tx, true) },
                                                enabled = !isBusy,
                                                colors = ButtonDefaults.buttonColors(containerColor = AnqeznyEmerald),
                                                modifier = Modifier.weight(1f)
                                            ) { Text(if (tx.type == "topup") "تأكيد الشحن" else "تم التحويل") }
                                            OutlinedButton(
                                                onClick = { onProcessWalletTx(tx, false) },
                                                enabled = !isBusy,
                                                modifier = Modifier.weight(1f)
                                            ) { Text("رفض", color = AnqeznyCrimson) }
                                        }
                                    } else {
                                        Text(
                                            if (tx.status == "approved") "تم التنفيذ" else "مرفوض",
                                            color = if (tx.status == "approved") AnqeznyEmerald else AnqeznyCrimson,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                4 -> {
                    var diesel by remember(pricingConfig) { mutableStateOf(pricingConfig.dieselPriceEgp.toString()) }
                    var baseFare by remember(pricingConfig) { mutableStateOf(pricingConfig.baseFareEgp.toString()) }
                    var perKm by remember(pricingConfig) { mutableStateOf(pricingConfig.perKmEgp.toString()) }
                    var comm by remember(pricingConfig) { mutableStateOf(pricingConfig.commissionPercent.toString()) }
                    var minWallet by remember(pricingConfig) { mutableStateOf(pricingConfig.minWalletEgp.toString()) }
                    var cancelFee by remember(pricingConfig) { mutableStateOf(pricingConfig.cancelFeeEgp.toString()) }
                    var gatewayLive by remember(pricingConfig) { mutableStateOf(pricingConfig.gatewayLive) }

                    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("إعدادات التسعير والعمولة والمحفظة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            OutlinedTextField(value = diesel, onValueChange = { diesel = it }, label = { Text("سعر لتر السولار (ج.م)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = baseFare, onValueChange = { baseFare = it }, label = { Text("فتح العداد الأساسي (ج.م)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = perKm, onValueChange = { perKm = it }, label = { Text("سعر الكيلومتر (ج.م)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = comm, onValueChange = { comm = it }, label = { Text("نسبة عمولة المنصة (%)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = minWallet, onValueChange = { minWallet = it }, label = { Text("الحد الأدنى لمحفظة الضمان (ج.م)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = cancelFee, onValueChange = { cancelFee = it }, label = { Text("رسوم الإلغاء بعد التحرك (ج.م)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())

                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("تفعيل بوابة الدفع الإلكتروني", fontWeight = FontWeight.Bold)
                                Switch(checked = gatewayLive, onCheckedChange = { gatewayLive = it })
                            }

                            Button(
                                onClick = {
                                    onSavePricing(
                                        AppPricingConfig(
                                            dieselPriceEgp = diesel.toDoubleOrNull() ?: 20.5,
                                            baseFareEgp = baseFare.toDoubleOrNull() ?: 185.0,
                                            perKmEgp = perKm.toDoubleOrNull() ?: 17.5,
                                            commissionPercent = comm.toDoubleOrNull() ?: 15.0,
                                            minWalletEgp = minWallet.toDoubleOrNull() ?: 200.0,
                                            cancelFeeEgp = cancelFee.toDoubleOrNull() ?: 40.0,
                                            minWithdrawEgp = pricingConfig.minWithdrawEgp,
                                            gatewayLive = gatewayLive
                                        )
                                    )
                                },
                                enabled = !isBusy,
                                colors = ButtonDefaults.buttonColors(containerColor = AnqeznyCrimson),
                                modifier = Modifier.fillMaxWidth().testTag("save_pricing_button")
                            ) {
                                Text("حفظ وتحديث الأسعار لحظياً")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun statusAr(s: String): String = when (s) {
    "approved" -> "موثق"
    "rejected" -> "مرفوض"
    "suspended" -> "موقوف"
    else -> "قيد المراجعة"
}

private fun statusColor(s: String): Color = when (s) {
    "approved" -> AnqeznyEmerald
    "rejected", "suspended" -> AnqeznyCrimson
    else -> AnqeznyAmber
}
