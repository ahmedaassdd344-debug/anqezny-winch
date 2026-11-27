package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.auth.authStateFlow
import com.example.auth.signOut
import com.example.data.model.AppPricingConfig
import com.example.data.repository.AnqeznyRepository
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.CompanyScreen
import com.example.ui.screens.CustomerScreen
import com.example.ui.screens.DriverScreen
import com.example.ui.screens.GoogleAuthSignInScreen
import com.example.ui.screens.RoleSelectionScreen
import com.example.ui.theme.AnqeznyCharcoal
import com.example.ui.theme.AnqeznyTheme
import com.example.ui.viewmodel.AnqeznyViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                AnqeznyTheme {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser
    if (user == null) {
        GoogleAuthSignInScreen(onAuthSuccess = {})
    } else {
        key(user.uid) {
            AnqeznyAuthenticatedShell(currentUserId = user.uid)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnqeznyAuthenticatedShell(currentUserId: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val viewModel: AnqeznyViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                AnqeznyViewModel(AnqeznyRepository(db), currentUserId)
            }
        }
    )

    val userProfileState by viewModel.userProfileState.collectAsStateWithLifecycle()
    val driverProfileState by viewModel.driverProfileState.collectAsStateWithLifecycle()
    val driverPrivateDocsState by viewModel.driverPrivateDocsState.collectAsStateWithLifecycle()
    val companyProfileState by viewModel.companyProfileState.collectAsStateWithLifecycle()
    val companyPrivateDocsState by viewModel.companyPrivateDocsState.collectAsStateWithLifecycle()
    val pricingConfigState by viewModel.pricingConfigState.collectAsStateWithLifecycle()
    val approvedDriversState by viewModel.approvedDriversState.collectAsStateWithLifecycle()
    val customerTripsState by viewModel.customerTripsState.collectAsStateWithLifecycle()
    val pendingTripsState by viewModel.pendingTripsState.collectAsStateWithLifecycle()
    val driverTripsState by viewModel.driverTripsState.collectAsStateWithLifecycle()
    val myDriverReviewsState by viewModel.myDriverReviewsState.collectAsStateWithLifecycle()
    val driverWalletTxState by viewModel.driverWalletTransactionsState.collectAsStateWithLifecycle()
    val companyDriversState by viewModel.companyDriversState.collectAsStateWithLifecycle()
    val companyInvitesState by viewModel.companyInvitesState.collectAsStateWithLifecycle()
    val companyTripsState by viewModel.companyTripsState.collectAsStateWithLifecycle()
    val tripMessagesState by viewModel.selectedTripMessagesState.collectAsStateWithLifecycle()
    val driverReviewsState by viewModel.selectedDriverReviewsState.collectAsStateWithLifecycle()
    val adminDriversState by viewModel.adminAllDriversState.collectAsStateWithLifecycle()
    val adminCompaniesState by viewModel.adminAllCompaniesState.collectAsStateWithLifecycle()
    val adminTripsState by viewModel.adminAllTripsState.collectAsStateWithLifecycle()
    val adminWalletTxState by viewModel.adminAllWalletTxState.collectAsStateWithLifecycle()

    val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.feedbackMessage.collectAsStateWithLifecycle()
    val activeScreenMode by viewModel.activeScreenMode.collectAsStateWithLifecycle()
    val isAdminVerified by viewModel.isAdminVerified.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(feedbackMessage) {
        val msg = feedbackMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    val userProfile = (userProfileState as? UiState.Success)?.data
    val driverProfile = (driverProfileState as? UiState.Success)?.data
    val driverPrivateDocs = (driverPrivateDocsState as? UiState.Success)?.data
    val companyProfile = (companyProfileState as? UiState.Success)?.data
    val companyPrivateDocs = (companyPrivateDocsState as? UiState.Success)?.data
    val pricingConfig = (pricingConfigState as? UiState.Success)?.data ?: AppPricingConfig()

    val effectiveMode = activeScreenMode ?: userProfile?.role ?: "role_select"

    if (effectiveMode != "role_select") {
        BackHandler { viewModel.setScreenMode("role_select") }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AnqeznyCharcoal,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                navigationIcon = {
                    if (effectiveMode != "role_select") {
                        IconButton(onClick = { viewModel.setScreenMode("role_select") }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                        }
                    }
                },
                                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("انقذنى عايز ونش", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = when (effectiveMode) {
                                "customer" -> "عميل"
                                "driver" -> "صفحة السائق"
                                "company" -> "صفحة الشركة"
                                "admin" -> "الإدارة"
                                else -> "الجيزة"
                            },
                            color = Color(0xFFFCA5A5),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                },
                actions = {
                    if (effectiveMode != "role_select") {
                        TextButton(onClick = { viewModel.setScreenMode("role_select") }) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = "رجوع للرئيسية", tint = Color.White)
                                Text("الرئيسية", color = Color.White)
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (effectiveMode) {
                "customer" -> {
                    val userPhoneDigits = userProfile?.phone?.filter { it.isDigit() }?.takeLast(10).orEmpty()
                    val allAdminTrips = (adminTripsState as? UiState.Success)?.data.orEmpty()
                    val directTrips = (customerTripsState as? UiState.Success)?.data.orEmpty()
                    val combinedCustomerTrips = if (userPhoneDigits.length >= 8) {
                        val byPhone = allAdminTrips.filter { t ->
                            t.customerPhone.filter { it.isDigit() }.endsWith(userPhoneDigits)
                        }
                        UiState.Success((directTrips + byPhone).distinctBy { it.id })
                    } else {
                        customerTripsState
                    }
                    CustomerScreen(
                        userProfile = userProfile,
                        pricingConfig = pricingConfig,
                        tripsState = combinedCustomerTrips,
                        approvedDriversState = approvedDriversState,
                        tripMessagesState = tripMessagesState,
                        driverReviewsState = driverReviewsState,
                        isBusy = isBusy,
                        onWatchTripMessages = viewModel::watchTripMessages,
                        onWatchDriverReviews = viewModel::watchDriverReviews,
                        onCreateTrip = { name, phone, pArea, pLat, pLng, dArea, dLat, dLng, dist, veh, svc, sched, pay, gw, fare, comm, net ->
                            viewModel.createTrip(
                                name, phone, pArea, pLat, pLng, dArea, dLat, dLng, dist,
                                veh, svc, sched, pay, gw, fare, comm, net, assignedDriver = null
                            )
                        },
                        onCancelTrip = viewModel::cancelTrip,
                        onSendMessage = viewModel::sendTripMessage,
                        onSubmitRating = viewModel::submitRatingAndReview,
                        onShowMessage = viewModel::showFeedback
                    )
                }

                "driver" -> DriverScreen(
                    driver = driverProfile ?: (adminDriversState as? UiState.Success)?.data?.firstOrNull { it.name == userProfile?.displayName } ?: (approvedDriversState as? UiState.Success)?.data?.firstOrNull { it.name == userProfile?.displayName } ?: (approvedDriversState as? UiState.Success)?.data?.firstOrNull(),
                    driverPrivateDocs = driverPrivateDocs,
                    myReviewsState = myDriverReviewsState,
                    pricingConfig = pricingConfig,
                    pendingTripsState = pendingTripsState,
                    driverTripsState = driverTripsState,
                    walletTxState = driverWalletTxState,
                    tripMessagesState = tripMessagesState,
                    isBusy = isBusy,
                    onWatchTripMessages = viewModel::watchTripMessages,
                    onToggleOnline = viewModel::toggleDriverOnline,
                    onAcceptTrip = viewModel::acceptTrip,
                    onMarkArrived = viewModel::markArrived,
                    onSavePhotosAndPin = viewModel::savePhotosAndVerifyPin,
                    onCompleteTrip = viewModel::completeTrip,
                    onCancelTrip = viewModel::cancelTrip,
                    onSendMessage = viewModel::sendTripMessage,
                    onRequestWalletTx = viewModel::requestWalletTransaction,
                    onResubmitDocs = { viewModel.setScreenMode("role_select") },
                    onSwitchToCustomer = { viewModel.setScreenMode("customer") },
                    onShowMessage = viewModel::showFeedback
                )

                "company" -> CompanyScreen(
                    company = companyProfile,
                    companyPrivateDocs = companyPrivateDocs,
                    driversState = companyDriversState,
                    invitesState = companyInvitesState,
                    tripsState = companyTripsState,
                    isBusy = isBusy,
                    onAddDriverInvite = viewModel::addCompanyDriverInvite,
                    onResubmitDocs = { viewModel.setScreenMode("role_select") },
                    onShowMessage = viewModel::showFeedback
                )

                "admin" -> AdminScreen(
                    pricingConfig = pricingConfig,
                    driversState = adminDriversState,
                    companiesState = adminCompaniesState,
                    tripsState = adminTripsState,
                    walletTxState = adminWalletTxState,
                    isBusy = isBusy,
                    onDispatchPhoneTrip = { name, phone, pArea, pLat, pLng, dArea, dLat, dLng, dist, veh, svc, fare, comm, net, drv ->
                        viewModel.createTrip(
                            name, phone, pArea, pLat, pLng, dArea, dLat, dLng, dist,
                            veh, svc, null, "كاش", pricingConfig.gatewayLive, fare, comm, net, assignedDriver = drv
                        )
                    },
                    onUpdateDriverStatus = viewModel::adminUpdateDriverStatus,
                    onUpdateCompanyStatus = viewModel::adminUpdateCompanyStatus,
                    onProcessWalletTx = viewModel::adminProcessWalletTx,
                    onSavePricing = viewModel::adminSavePricing,
                    onShowMessage = viewModel::showFeedback
                )

                else -> RoleSelectionScreen(
                    isBusy = isBusy,
                    isAdminVerified = isAdminVerified,
                    existingUserProfile = userProfile,
                    existingDriver = driverProfile,
                    existingDriverDocs = driverPrivateDocs,
                    existingCompany = companyProfile,
                    existingCompanyDocs = companyPrivateDocs,
                    onEnterCustomer = viewModel::enterAsCustomer,
                    onResumeDriver = viewModel::resumeDriverPortal,
                    onRegisterDriver = viewModel::registerDriver,
                    onResumeCompany = viewModel::resumeCompanyPortal,
                    onRegisterCompany = viewModel::registerCompany,
                    onEnterAdmin = { viewModel.setScreenMode("admin") },
                    onShowMessage = viewModel::showFeedback
                )
            }
        }
    }
}
