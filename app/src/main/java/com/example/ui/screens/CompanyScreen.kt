package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.CompanyDriverInvite
import com.example.data.model.CompanyPrivateDocs
import com.example.data.model.CompanyProfile
import com.example.data.model.DriverProfile
import com.example.data.model.GIZA_AREAS
import com.example.data.model.Trip
import com.example.ui.components.TermsDialog
import com.example.ui.theme.AnqeznyAmber
import com.example.ui.theme.AnqeznyBackground
import com.example.ui.theme.AnqeznyCharcoal
import com.example.ui.theme.AnqeznyCrimson
import com.example.ui.theme.AnqeznyEmerald
import com.example.ui.theme.AnqeznyInk
import com.example.ui.theme.AnqeznyInkSecondary
import com.example.ui.theme.AnqeznyStarGold
import com.example.ui.viewmodel.UiState
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyScreen(
    company: CompanyProfile?,
    companyPrivateDocs: CompanyPrivateDocs?,
    driversState: UiState<List<DriverProfile>>,
    invitesState: UiState<List<CompanyDriverInvite>>,
    tripsState: UiState<List<Trip>>,
    isBusy: Boolean,
    onAddDriverInvite: (companyName: String, driverName: String, driverPhone: String, plate: String, winchType: String, area: String) -> Unit,
    onResubmitDocs: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (company == null || company.status != "approved") {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(AnqeznyBackground)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(shape = RoundedCornerShape(20.dp), color = Color.White, modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth()) {
                Column(modifier = Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(
                        imageVector = if (company?.status == "rejected") Icons.Default.Warning else Icons.Default.HourglassTop,
                        contentDescription = null,
                        tint = if (company?.status == "rejected") AnqeznyCrimson else AnqeznyAmber,
                        modifier = Modifier.size(52.dp)
                    )
                    Text(
                        text = if (company?.status == "rejected") "تم رفض أوراق الشركة" else "أوراق الشركة تحت المراجعة",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = company?.rejectionReason ?: "تتم مراجعة السجل التجاري والبطاقة الضريبية للشركة، وستُفتح لوحة الشركة تلقائياً فور الاعتماد.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AnqeznyInkSecondary
                    )
                    if (company?.status == "rejected") {
                        Button(onClick = onResubmitDocs, colors = ButtonDefaults.buttonColors(containerColor = AnqeznyCrimson)) {
                            Text("إعادة إرسال الأوراق")
                        }
                    }
                }
            }
        }
        return
    }

    val clipboardManager = LocalClipboardManager.current
    var showTerms by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var dName by remember { mutableStateOf("") }
    var dPhone by remember { mutableStateOf("") }
    var dPlate by remember { mutableStateOf("") }
    var dWinch by remember { mutableStateOf("ونش هيدروليك مسطح") }
    var dArea by remember { mutableStateOf(company.area.ifBlank { GIZA_AREAS.first().nameAr }) }
    var areaExpanded by remember { mutableStateOf(false) }

    if (showTerms) {
        TermsDialog(onDismiss = { showTerms = false })
    }

    val drivers = (driversState as? UiState.Success)?.data.orEmpty()
    val invites = (invitesState as? UiState.Success)?.data.orEmpty()
    val trips = (tripsState as? UiState.Success)?.data.orEmpty()
        .sortedByDescending { it.createdAt?.seconds ?: 0L }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AnqeznyBackground),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(color = AnqeznyCharcoal, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(company.companyName, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("كود ربط السائقين: ${company.userId}", color = Color(0xFFCBD5E1), style = MaterialTheme.typography.labelMedium)
            }
        }

        TabRow(selectedTabIndex = selectedTab, containerColor = Color.White, contentColor = AnqeznyCrimson) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("السائقون (${drivers.size + invites.size})") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("الرحلات (${trips.size})") })
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("صفحة الشركة") })
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
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("إضافة سائق تابع للشركة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        OutlinedTextField(value = dName, onValueChange = { dName = it }, label = { Text("اسم السائق") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(
                            value = dPhone,
                            onValueChange = { dPhone = it },
                            label = { Text("رقم الموبايل") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = dPlate, onValueChange = { dPlate = it }, label = { Text("رقم اللوحة") }, singleLine = true, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = dWinch, onValueChange = { dWinch = it }, label = { Text("نوع الونش") }, singleLine = true, modifier = Modifier.weight(1f))
                        }
                        ExposedDropdownMenuBox(expanded = areaExpanded, onExpandedChange = { areaExpanded = !areaExpanded }) {
                            OutlinedTextField(
                                value = dArea,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("منطقة التشغيل في الجيزة") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = areaExpanded) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = areaExpanded, onDismissRequest = { areaExpanded = false }) {
                                GIZA_AREAS.forEach { a ->
                                    DropdownMenuItem(text = { Text(a.nameAr) }, onClick = { dArea = a.nameAr; areaExpanded = false })
                                }
                            }
                        }
                        Button(
                            onClick = {
                                if (dName.trim().length < 2 || dPhone.trim().length < 6 || dPlate.trim().length < 2) {
                                    onShowMessage("أكمل بيانات السائق أولاً")
                                } else {
                                    onAddDriverInvite(company.companyName, dName, dPhone, dPlate, dWinch, dArea)
                                    dName = ""; dPhone = ""; dPlate = ""
                                }
                            },
                            enabled = !isBusy,
                            colors = ButtonDefaults.buttonColors(containerColor = AnqeznyCrimson),
                            modifier = Modifier.fillMaxWidth().testTag("company_add_driver_button")
                        ) {
                            Text("إضافة السائق")
                        }
                    }
                }

                drivers.forEach { drv ->
                    Surface(shape = RoundedCornerShape(14.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(drv.name, fontWeight = FontWeight.Bold)
                                Text(if (drv.online) "متصل" else "غير متصل", color = if (drv.online) AnqeznyEmerald else AnqeznyInkSecondary)
                            }
                            Text("${drv.winchType} • لوحة: ${drv.plateNumber} • ${drv.area}", style = MaterialTheme.typography.bodyMedium, color = AnqeznyInkSecondary)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("محفظة: ${drv.walletBalance.roundToInt()} ج.م • رحلات: ${drv.totalTrips}", style = MaterialTheme.typography.labelMedium)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, null, tint = AnqeznyStarGold, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(3.dp))
                                    Text(if (drv.ratingCount > 0) String.format(Locale.US, "%.1f", drv.ratingAvg) else "جديد", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }

                invites.forEach { inv ->
                    Surface(shape = RoundedCornerShape(14.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(inv.driverName, fontWeight = FontWeight.Bold)
                                Text("${inv.driverPhone} • ${inv.plateNumber} • ${inv.area}", style = MaterialTheme.typography.labelMedium, color = AnqeznyInkSecondary)
                            }
                            Text("بانتظار الربط", color = AnqeznyAmber, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            } else if (selectedTab == 1) {
                val completed = trips.filter { it.status == "completed" }
                val totalFare = completed.sumOf { it.fareEgp }
                val totalComm = completed.sumOf { it.commissionEgp }
                val totalNet = completed.sumOf { it.driverNetEgp }

                Surface(shape = RoundedCornerShape(16.dp), color = AnqeznyCharcoal, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${totalFare.roundToInt()} ج.م", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("تحصيل الشركة", color = Color(0xFF94A3B8), style = MaterialTheme.typography.labelMedium)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${totalComm.roundToInt()} ج.م", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("عمولة المنصة", color = Color(0xFF94A3B8), style = MaterialTheme.typography.labelMedium)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${totalNet.roundToInt()} ج.م", color = Color(0xFF6EE7B7), fontWeight = FontWeight.Bold)
                            Text("صافي السائقين", color = Color(0xFF94A3B8), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                trips.forEach { t ->
                    Surface(shape = RoundedCornerShape(14.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${t.pickupArea} ← ${t.dropoffArea}", fontWeight = FontWeight.Bold)
                                Text("${t.fareEgp.roundToInt()} ج.م", color = AnqeznyCrimson, fontWeight = FontWeight.Bold)
                            }
                            Text("السائق: ${t.driverName ?: "-"} • الحالة: ${tripStatusAr(t.status)}", style = MaterialTheme.typography.labelMedium, color = AnqeznyInkSecondary)
                        }
                    }
                }
            } else {
                Surface(shape = RoundedCornerShape(18.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, null, tint = AnqeznyEmerald)
                            Spacer(Modifier.width(6.dp))
                            Text(company.companyName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                        Text("المسؤول المفوض: ${company.managerName}", color = AnqeznyInk, fontWeight = FontWeight.Bold)
                        Text("رقم التواصل: ${companyPrivateDocs?.phone ?: "-"}", color = AnqeznyInkSecondary)
                        Text("السجل التجاري: ${company.commercialReg} • البطاقة الضريبية: ${company.taxCard}", color = AnqeznyInkSecondary)
                        Text("العنوان: ${company.address} (${company.area})", color = AnqeznyInkSecondary)

                        Spacer(Modifier.height(6.dp))
                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(company.userId))
                                onShowMessage("تم نسخ كود ربط السائقين: ${company.userId}")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AnqeznyCharcoal),
                            modifier = Modifier.fillMaxWidth().testTag("copy_company_code_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("نسخ كود ربط السائقين (${company.userId.take(8)}...)")
                        }
                        androidx.compose.material3.OutlinedButton(
                            onClick = { showTerms = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("عرض الشروط والأحكام")
                        }
                    }
                }
            }
        }
    }
}
