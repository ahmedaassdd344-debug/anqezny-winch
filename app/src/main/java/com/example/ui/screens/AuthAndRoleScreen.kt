package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.auth.attemptAutoSignIn
import com.example.auth.onGoogleSignInClicked
import com.example.data.model.CompanyPrivateDocs
import com.example.data.model.CompanyProfile
import com.example.data.model.DriverPrivateDocs
import com.example.data.model.DriverProfile
import com.example.data.model.GIZA_AREAS
import com.example.data.model.UserProfile
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
import com.example.util.encodeUriToBase64Thumb
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.imePadding

@Composable
fun GoogleAuthSignInScreen(
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var errorMsg by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        Firebase.auth.signInAnonymously()
            .addOnSuccessListener { onAuthSuccess() }
            .addOnFailureListener { e -> errorMsg = e.localizedMessage }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AnqeznyBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_winch_1795484630717),
                        contentDescription = "ونش إنقاذ انقذنى عايز ونش في الجيزة",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "انقذنى عايز ونش",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = AnqeznyInk
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "جاري الدخول المباشر للمنصة...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AnqeznyInkSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(18.dp))
                    if (errorMsg != null) {
                        Text(text = errorMsg ?: "", color = AnqeznyCrimson, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = {
                                errorMsg = null
                                Firebase.auth.signInAnonymously()
                                    .addOnSuccessListener { onAuthSuccess() }
                                    .addOnFailureListener { e -> errorMsg = e.localizedMessage }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AnqeznyCrimson),
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            Text("دخول مباشر الآن", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        CircularProgressIndicator(color = AnqeznyCrimson)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectionScreen(
    isBusy: Boolean,
    isAdminVerified: Boolean,
    existingUserProfile: UserProfile?,
    existingDriver: DriverProfile?,
    existingDriverDocs: DriverPrivateDocs?,
    existingCompany: CompanyProfile?,
    existingCompanyDocs: CompanyPrivateDocs?,
    onEnterCustomer: (name: String, phone: String) -> Unit,
    onResumeDriver: (name: String, phone: String) -> Unit,
    onRegisterDriver: (
        name: String, phone: String, nationalId: String, licenseNumber: String,
        licenseExpiry: String, winchType: String, plateNumber: String, area: String,
        companyId: String?, companyName: String?,
        docIdThumb: String?, docDriverLicenseThumb: String?, docVehicleLicenseThumb: String?, docTruckPhotoThumb: String?
    ) -> Unit,
    onResumeCompany: (name: String, phone: String) -> Unit,
    onRegisterCompany: (
        companyName: String, managerName: String, phone: String, commercialReg: String,
        taxCard: String, address: String, area: String,
        docRegThumb: String?, docTaxThumb: String?, docIdThumb: String?, docOfficeThumb: String?
    ) -> Unit,
    onEnterAdmin: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authUser = Firebase.auth.currentUser

    // "choice", "customer", "partner_register"
    var selectedEntry by remember { mutableStateOf("choice") }
    var partnerType by remember { mutableIntStateOf(0) } // 0 = driver, 1 = company

    if (selectedEntry != "choice") {
        BackHandler { selectedEntry = "choice" }
    }

    var phone by remember(existingUserProfile, existingDriverDocs, existingCompanyDocs) {
        mutableStateOf(
            existingDriverDocs?.phone?.ifBlank { null }
                ?: existingCompanyDocs?.phone?.ifBlank { null }
                ?: existingUserProfile?.phone
                ?: ""
        )
    }
    var acceptedTerms by remember(existingUserProfile) {
        mutableStateOf(existingUserProfile?.acceptedTerms ?: false)
    }
    var showTerms by remember { mutableStateOf(false) }

    // Customer quick entry
    var customerName by remember(existingUserProfile, authUser) {
        mutableStateOf(
            existingUserProfile?.displayName?.ifBlank { null }
                ?: authUser?.displayName
                ?: ""
        )
    }

    // Quick Login Name/Phone for returning Driver
    var quickDriverName by remember(existingDriver) {
        mutableStateOf(existingDriver?.name ?: authUser?.displayName ?: "")
    }
    var quickDriverPhone by remember(existingDriverDocs) {
        mutableStateOf(existingDriverDocs?.phone ?: "")
    }

    // Driver first-time registration fields
    var driverName by remember(existingDriver, authUser) {
        mutableStateOf(existingDriver?.name ?: authUser?.displayName ?: "")
    }
    var nationalId by remember(existingDriverDocs) { mutableStateOf(existingDriverDocs?.nationalId ?: "") }
    var licenseNumber by remember(existingDriverDocs) { mutableStateOf(existingDriverDocs?.licenseNumber ?: "") }
    var licenseExpiry by remember(existingDriverDocs) { mutableStateOf(existingDriverDocs?.licenseExpiry?.ifBlank { "2028-12" } ?: "2028-12") }
    var winchType by remember(existingDriver) { mutableStateOf(existingDriver?.winchType?.ifBlank { "ونش هيدروليك مسطح" } ?: "ونش هيدروليك مسطح") }
    var plateNumber by remember(existingDriver) { mutableStateOf(existingDriver?.plateNumber ?: "") }
    var selectedArea by remember(existingDriver) { mutableStateOf(existingDriver?.area?.ifBlank { GIZA_AREAS.first().nameAr } ?: GIZA_AREAS.first().nameAr) }
    var areaExpanded by remember { mutableStateOf(false) }
    var companyCode by remember(existingDriver) { mutableStateOf(existingDriver?.companyId ?: "") }
    var docIdThumb by remember { mutableStateOf<String?>(null) }
    var docDriverLicenseThumb by remember { mutableStateOf<String?>(null) }
    var docVehicleLicenseThumb by remember { mutableStateOf<String?>(null) }
    var docTruckPhotoThumb by remember { mutableStateOf<String?>(null) }

    // Company first-time registration fields
    var companyName by remember(existingCompany) { mutableStateOf(existingCompany?.companyName ?: "") }
    var managerName by remember(existingCompany, authUser) { mutableStateOf(existingCompany?.managerName ?: authUser?.displayName ?: "") }
    var commercialReg by remember(existingCompany) { mutableStateOf(existingCompany?.commercialReg ?: "") }
    var taxCard by remember(existingCompany) { mutableStateOf(existingCompany?.taxCard ?: "") }
    var companyAddress by remember(existingCompany) { mutableStateOf(existingCompany?.address ?: "") }
    var docRegThumb by remember { mutableStateOf<String?>(null) }
    var docTaxThumb by remember { mutableStateOf<String?>(null) }
    var docManagerIdThumb by remember { mutableStateOf<String?>(null) }
    var docOfficeThumb by remember { mutableStateOf<String?>(null) }

    var activeUploadSlot by remember { mutableStateOf("") }
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val encoded = encodeUriToBase64Thumb(context, uri, 320)
                if (encoded != null) {
                    when (activeUploadSlot) {
                        "d_id" -> docIdThumb = encoded
                        "d_lic" -> docDriverLicenseThumb = encoded
                        "d_veh" -> docVehicleLicenseThumb = encoded
                        "d_truck" -> docTruckPhotoThumb = encoded
                        "c_reg" -> docRegThumb = encoded
                        "c_tax" -> docTaxThumb = encoded
                        "c_id" -> docManagerIdThumb = encoded
                        "c_office" -> docOfficeThumb = encoded
                    }
                    onShowMessage("تم إرفاق المستند بنجاح")
                } else {
                    onShowMessage("تعذر قراءة الصورة المختارة")
                }
            }
        }
    }

    if (showTerms) {
        TermsDialog(
            onDismiss = { showTerms = false },
            onAccept = {
                acceptedTerms = true
                showTerms = false
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AnqeznyBackground),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                            .imePadding()
            .verticalScroll(rememberScrollState())

                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
                        // Header card (5 secret taps on title opens Admin Portal!)
            var secretAdminTaps by remember { mutableIntStateOf(0) }
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AnqeznyCharcoal,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "انقذنى عايز ونش — الجيزة",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            secretAdminTaps++
                            if (secretAdminTaps >= 5) {
                                secretAdminTaps = 0
                                onEnterAdmin()
                            }
                        }
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "أهلاً بك • اطلب أقرب ونش إنقاذ في الجيزة خلال ثوانٍ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }

            if (selectedEntry == "choice") {
                // Card 1: Instant Customer Action (1-tap if already registered!)
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    border = BorderStroke(1.5.dp, AnqeznyCrimson),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (existingUserProfile != null && existingUserProfile.phone.isNotBlank()) {
                                onEnterCustomer(existingUserProfile.displayName, existingUserProfile.phone)
                            } else {
                                selectedEntry = "customer"
                            }
                        }
                        .testTag("choose_customer_role")
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(AnqeznyCrimsonContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = AnqeznyCrimson, modifier = Modifier.size(32.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("عايز ونش فوراً (عميل)", style = MaterialTheme.typography.titleLarge, color = AnqeznyInk, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (existingUserProfile != null && existingUserProfile.phone.isNotBlank()) {
                                    "دخول مباشر بضغطة واحدة لطلب أقرب ونش في الجيزة"
                                } else {
                                    "اكتب اسمك ورقمك واطلب أقرب ونش في الجيزة خلال ثوانٍ"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = AnqeznyInkSecondary
                            )
                        }
                    }
                }

                               // Dedicated Returning Driver Portal Card (ALWAYS VISIBLE for quick phone login!)
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    border = BorderStroke(
                        1.5.dp,
                        if (existingDriver?.status == "approved") AnqeznyEmerald else AnqeznyCharcoal
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = if (existingDriver?.status == "approved") AnqeznyEmerald else AnqeznyCharcoal
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "دخول السائقين المسجلين",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (existingDriver != null) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (existingDriver.status == "approved") AnqeznyEmeraldContainer else AnqeznyAmberContainer
                                ) {
                                    Text(
                                        text = when (existingDriver.status) {
                                            "approved" -> "حساب معتمد"
                                            "rejected" -> "مرفوض"
                                            "suspended" -> "موقوف"
                                            else -> "تحت المراجعة"
                                        },
                                        color = if (existingDriver.status == "approved") AnqeznyEmerald else AnqeznyAmber,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        if (existingDriver != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccountBalanceWallet, null, tint = AnqeznyCrimson, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("محفظة الضمان: ${existingDriver.walletBalance.roundToInt()} ج.م", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, null, tint = AnqeznyStarGold, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = if (existingDriver.ratingCount > 0) String.format(Locale.US, " %.1f", existingDriver.ratingAvg) else " جديد",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "لو سجلت أوراقك قبل كده، اكتب رقم موبايلك وادخل فوراً لاستقبال الطلبات",
                                style = MaterialTheme.typography.bodySmall,
                                color = AnqeznyInkSecondary
                            )
                        }

                        OutlinedTextField(
                            value = quickDriverPhone,
                            onValueChange = { quickDriverPhone = it },
                            label = { Text("رقم موبايل السائق المسجل (01xxxxxxxxx)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                val finalPhone = quickDriverPhone.trim().ifBlank { existingDriverDocs?.phone ?: "" }
                                val finalName = quickDriverName.trim().ifBlank { existingDriver?.name ?: "كابتن ونش" }
                                if (finalPhone.length < 10) {
                                    onShowMessage("أدخل رقم الموبايل المسجل للدخول (11 رقم)")
                                } else {
                                    onResumeDriver(finalName, finalPhone)
                                }
                            },
                            enabled = !isBusy,
                            colors = ButtonDefaults.buttonColors(containerColor = AnqeznyCharcoal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("resume_driver_button")
                        ) {
                            Text("دخول للوحة السائق واستقبال الطلبات", fontWeight = FontWeight.Bold)
                        }

                        if (existingDriver?.status == "rejected") {
                            TextButton(
                                onClick = {
                                    partnerType = 0
                                    selectedEntry = "partner_register"
                                },
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text("تعديل وإعادة إرسال الأوراق", color = AnqeznyCrimson)
                            }
                        }
                    }
                }

                // Dedicated Returning Company Portal Card (if company already registered once!)
                if (existingCompany != null) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White,
                        border = BorderStroke(1.5.dp, AnqeznyCharcoal.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("صفحة الشركة: ${existingCompany.companyName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (existingCompany.status == "approved") "شركة معتمدة" else "تحت المراجعة",
                                    color = if (existingCompany.status == "approved") AnqeznyEmerald else AnqeznyAmber,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                            Text(
                                text = "المسؤول: ${existingCompany.managerName} • ${existingCompanyDocs?.phone ?: ""}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = AnqeznyInkSecondary
                            )
                            Button(
                                onClick = {
                                    onResumeCompany(
                                        existingCompany.managerName,
                                        existingCompanyDocs?.phone ?: existingUserProfile?.phone ?: ""
                                    )
                                },
                                enabled = !isBusy,
                                colors = ButtonDefaults.buttonColors(containerColor = AnqeznyCharcoal),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("resume_company_button")
                            ) {
                                Text("دخول لصفحة الشركة والأسطول")
                            }
                        }
                    }
                }

                // First-time Partner Registration Card (hidden if already has both, or shown cleanly for new partners)
                if (existingDriver == null || existingCompany == null) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                partnerType = if (existingDriver != null) 1 else 0
                                selectedEntry = "partner_register"
                            }
                            .testTag("choose_partner_role")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFE2E8F0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = AnqeznyCharcoal, modifier = Modifier.size(28.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (existingDriver == null) "تسجيل سائق جديد أو شركة تشغيل (أول مرة)" else "تسجيل شركة / مكتب تشغيل",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AnqeznyInk,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "تسجل بياناتك وأوراقك مرة واحدة فقط، وبعد الاعتماد تدخل مباشرة باسمك ورقمك.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AnqeznyInkSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))
                
            } else if (selectedEntry == "customer") {
                // Ultra-fast First-time Customer Entry (NO OTP!)
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("طلب ونش سريع (عميل)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            TextButton(onClick = { selectedEntry = "choice" }) { Text("رجوع") }
                        }

                        Text(
                            text = "اكتب اسمك ورقم موبايلك فقط للتواصل عند وصول الونش — بدون كود تحقق أو خطوات إضافية.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AnqeznyInkSecondary
                        )

                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = { Text("الاسم") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("customer_name_input")
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("رقم الموبايل المصري (01xxxxxxxxx)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("customer_phone_input")
                        )

                        TermsCheckboxRow(
                            accepted = acceptedTerms,
                            onCheckedChange = { acceptedTerms = it },
                            onOpenTerms = { showTerms = true }
                        )

                        Button(
                            onClick = {
                                when {
                                    customerName.trim().length < 2 -> onShowMessage("يرجى إدخال اسمك")
                                    phone.trim().length < 10 -> onShowMessage("يرجى إدخال رقم موبايل مصري صحيح")
                                    !acceptedTerms -> onShowMessage("يجب الموافقة على الشروط والأحكام")
                                    else -> onEnterCustomer(customerName.trim(), phone.trim())
                                }
                            },
                            enabled = !isBusy,
                            colors = ButtonDefaults.buttonColors(containerColor = AnqeznyCrimson),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("customer_continue_button")
                        ) {
                            Text("ادخل واطلب ونش فوراً", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            } else {
                // First-time Partner Registration (NO OTP!)
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("تسجيل لأول مرة", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            TextButton(onClick = { selectedEntry = "choice" }) { Text("رجوع") }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            FilterChip(
                                selected = partnerType == 0,
                                onClick = { partnerType = 0 },
                                label = { Text("سائق مستقل") },
                                leadingIcon = { Icon(Icons.Default.LocalShipping, null, modifier = Modifier.size(16.dp)) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("partner_tab_driver")
                            )
                            FilterChip(
                                selected = partnerType == 1,
                                onClick = { partnerType = 1 },
                                label = { Text("شركة / مكتب تشغيل") },
                                leadingIcon = { Icon(Icons.Default.Business, null, modifier = Modifier.size(16.dp)) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("partner_tab_company")
                            )
                        }

                        HorizontalDivider()

                        if (partnerType == 0) {
                            OutlinedTextField(
                                value = driverName,
                                onValueChange = { driverName = it },
                                label = { Text("اسم السائق ثلاثي") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("رقم الموبايل") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = nationalId,
                                onValueChange = { nationalId = it },
                                label = { Text("الرقم القومي (14 رقم)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = licenseNumber,
                                    onValueChange = { licenseNumber = it },
                                    label = { Text("رقم الرخصة") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = licenseExpiry,
                                    onValueChange = { licenseExpiry = it },
                                    label = { Text("تاريخ الانتهاء") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = winchType,
                                    onValueChange = { winchType = it },
                                    label = { Text("نوع الونش") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = plateNumber,
                                    onValueChange = { plateNumber = it },
                                    label = { Text("رقم اللوحة") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            ExposedDropdownMenuBox(
                                expanded = areaExpanded,
                                onExpandedChange = { areaExpanded = !areaExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedArea,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("منطقة التشغيل في الجيزة") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = areaExpanded) },
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                        .fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = areaExpanded,
                                    onDismissRequest = { areaExpanded = false }
                                ) {
                                    GIZA_AREAS.forEach { area ->
                                        DropdownMenuItem(
                                            text = { Text(area.nameAr) },
                                            onClick = {
                                                selectedArea = area.nameAr
                                                areaExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = companyCode,
                                onValueChange = { companyCode = it },
                                label = { Text("كود الشركة (اختياري إذا كنت تابعاً لشركة)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text("الأوراق القانونية المطلوبة (لأول مرة فقط — 4 صور):", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            DocUploadRow("1. صورة البطاقة الشخصية", docIdThumb != null) {
                                activeUploadSlot = "d_id"
                                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                            DocUploadRow("2. صورة رخصة القيادة", docDriverLicenseThumb != null) {
                                activeUploadSlot = "d_lic"
                                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                            DocUploadRow("3. صورة رخصة الونش / المركبة", docVehicleLicenseThumb != null) {
                                activeUploadSlot = "d_veh"
                                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                            DocUploadRow("4. صورة الونش", docTruckPhotoThumb != null) {
                                activeUploadSlot = "d_truck"
                                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }

                            TermsCheckboxRow(
                                accepted = acceptedTerms,
                                onCheckedChange = { acceptedTerms = it },
                                onOpenTerms = { showTerms = true }
                            )

                            Button(
                                onClick = {
                                    when {
                                        driverName.trim().length < 2 -> onShowMessage("يرجى إدخال اسم السائق")
                                        phone.trim().length < 10 -> onShowMessage("يرجى إدخال رقم موبايل صحيح")
                                        nationalId.trim().length < 8 -> onShowMessage("يرجى إدخال الرقم القومي")
                                        licenseNumber.trim().length < 3 -> onShowMessage("يرجى إدخال رقم الرخصة")
                                        licenseExpiry.trim().length < 4 -> onShowMessage("يرجى إدخال تاريخ انتهاء الرخصة")
                                        plateNumber.trim().length < 2 -> onShowMessage("يرجى إدخال رقم اللوحة")
                                        !acceptedTerms -> onShowMessage("يجب الموافقة على الشروط والأحكام")
                                        else -> onRegisterDriver(
                                            driverName.trim(), phone.trim(), nationalId.trim(),
                                            licenseNumber.trim(), licenseExpiry.trim(), winchType.trim(),
                                            plateNumber.trim(), selectedArea,
                                            companyCode.trim().takeIf { it.isNotEmpty() }, null,
                                            docIdThumb, docDriverLicenseThumb, docVehicleLicenseThumb, docTruckPhotoThumb
                                        )
                                    }
                                },
                                enabled = !isBusy,
                                colors = ButtonDefaults.buttonColors(containerColor = AnqeznyCrimson),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("register_driver_submit_button")
                            ) {
                                Text("إرسال الأوراق والتسجيل لأول مرة")
                            }
                        } else {
                            OutlinedTextField(
                                value = companyName,
                                onValueChange = { companyName = it },
                                label = { Text("اسم الشركة / مكتب التشغيل") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = managerName,
                                onValueChange = { managerName = it },
                                label = { Text("اسم المسؤول المفوض") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("رقم موبايل التواصل") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = commercialReg,
                                    onValueChange = { commercialReg = it },
                                    label = { Text("السجل التجاري") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = taxCard,
                                    onValueChange = { taxCard = it },
                                    label = { Text("البطاقة الضريبية") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            OutlinedTextField(
                                value = companyAddress,
                                onValueChange = { companyAddress = it },
                                label = { Text("عنوان المقر في الجيزة") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text("أوراق الشركة المطلوبة (لأول مرة فقط — 4 صور):", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            DocUploadRow("1. صورة السجل التجاري", docRegThumb != null) {
                                activeUploadSlot = "c_reg"
                                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                            DocUploadRow("2. صورة البطاقة الضريبية", docTaxThumb != null) {
                                activeUploadSlot = "c_tax"
                                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                            DocUploadRow("3. بطاقة المسؤول", docManagerIdThumb != null) {
                                activeUploadSlot = "c_id"
                                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                            DocUploadRow("4. صورة المقر / الأسطول", docOfficeThumb != null) {
                                activeUploadSlot = "c_office"
                                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }

                            TermsCheckboxRow(
                                accepted = acceptedTerms,
                                onCheckedChange = { acceptedTerms = it },
                                onOpenTerms = { showTerms = true }
                            )

                            Button(
                                onClick = {
                                    when {
                                        companyName.trim().length < 2 -> onShowMessage("يرجى إدخال اسم الشركة")
                                        managerName.trim().length < 2 -> onShowMessage("يرجى إدخال اسم المسؤول")
                                        phone.trim().length < 10 -> onShowMessage("يرجى إدخال رقم الموبايل")
                                        commercialReg.trim().length < 3 -> onShowMessage("يرجى إدخال السجل التجاري")
                                        taxCard.trim().length < 3 -> onShowMessage("يرجى إدخال البطاقة الضريبية")
                                        !acceptedTerms -> onShowMessage("يجب الموافقة على الشروط والأحكام")
                                        else -> onRegisterCompany(
                                            companyName.trim(), managerName.trim(), phone.trim(),
                                            commercialReg.trim(), taxCard.trim(),
                                            companyAddress.trim().ifBlank { selectedArea }, selectedArea,
                                            docRegThumb, docTaxThumb, docManagerIdThumb, docOfficeThumb
                                        )
                                    }
                                },
                                enabled = !isBusy,
                                colors = ButtonDefaults.buttonColors(containerColor = AnqeznyCharcoal),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("register_company_submit_button")
                            ) {
                                Text("تسجيل الشركة وإرسال الأوراق")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DocUploadRow(label: String, uploaded: Boolean, onPick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (uploaded) AnqeznyEmeraldContainer else Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, if (uploaded) AnqeznyEmerald else Color(0xFFCBD5E1)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = AnqeznyInk)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (uploaded) Icons.Default.CheckCircle else Icons.Default.UploadFile,
                    contentDescription = null,
                    tint = if (uploaded) AnqeznyEmerald else AnqeznyInkSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (uploaded) "تم الإرفاق" else "إرفاق صورة",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (uploaded) AnqeznyEmerald else AnqeznyInkSecondary
                )
            }
        }
    }
}

@Composable
private fun TermsCheckboxRow(
    accepted: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onOpenTerms: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(
            checked = accepted,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag("terms_checkbox")
        )
        Text("أوافق على ", style = MaterialTheme.typography.bodyMedium)
        Text(
            text = "الشروط والأحكام وسياسة الخصوصية",
            style = MaterialTheme.typography.bodyMedium,
            color = AnqeznyCrimson,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clickable(onClick = onOpenTerms)
                .padding(vertical = 4.dp)
        )
    }
}
