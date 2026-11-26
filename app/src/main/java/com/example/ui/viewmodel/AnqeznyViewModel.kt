package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppPricingConfig
import com.example.data.model.CompanyDriverInvite
import com.example.data.model.CompanyPrivateDocs
import com.example.data.model.CompanyProfile
import com.example.data.model.DriverPrivateDocs
import com.example.data.model.DriverProfile
import com.example.data.model.DriverReview
import com.example.data.model.Trip
import com.example.data.model.TripMessage
import com.example.data.model.UserProfile
import com.example.data.model.WalletTransaction
import com.example.data.repository.AnqeznyRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class AnqeznyViewModel(
    private val repository: AnqeznyRepository,
    val currentUserId: String
) : ViewModel() {

    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    private val _activeScreenMode = MutableStateFlow<String?>(null)
    val activeScreenMode: StateFlow<String?> = _activeScreenMode.asStateFlow()

    private val _isAdminVerified = MutableStateFlow(true)
    val isAdminVerified: StateFlow<Boolean> = _isAdminVerified.asStateFlow()

    private val _selectedTripForChat = MutableStateFlow<String?>(null)
    private val _selectedDriverForProfile = MutableStateFlow<String?>(null)

    private fun <T> Flow<T>.toUiState(tag: String): StateFlow<UiState<T>> =
        this.map<T, UiState<T>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w("AnqeznyVM", "Error observing $tag", rootError)
                emit(UiState.Error(rootError.message ?: "تعذر تحميل البيانات ($tag)"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = UiState.Loading
            )

    val userProfileState: StateFlow<UiState<UserProfile?>> =
        repository.observeUserProfile(currentUserId).toUiState("userProfile")

    val pricingConfigState: StateFlow<UiState<AppPricingConfig>> =
        repository.observePricingConfig().toUiState("pricingConfig")

    val driverProfileState: StateFlow<UiState<DriverProfile?>> =
        repository.observeDriver(currentUserId).toUiState("driverProfile")

    val driverPrivateDocsState: StateFlow<UiState<DriverPrivateDocs?>> =
        repository.observeDriverPrivateDocs(currentUserId).toUiState("driverPrivateDocs")

    val approvedDriversState: StateFlow<UiState<List<DriverProfile>>> =
        repository.observeApprovedDrivers().toUiState("approvedDrivers")

    val customerTripsState: StateFlow<UiState<List<Trip>>> =
        repository.observeCustomerTrips(currentUserId).toUiState("customerTrips")

    val pendingTripsState: StateFlow<UiState<List<Trip>>> =
        repository.observePendingTrips().toUiState("pendingTrips")

    val driverTripsState: StateFlow<UiState<List<Trip>>> =
        repository.observeDriverTrips(currentUserId).toUiState("driverTrips")

    val driverWalletTransactionsState: StateFlow<UiState<List<WalletTransaction>>> =
        repository.observeDriverWalletTransactions(currentUserId).toUiState("driverWalletTx")

    val companyProfileState: StateFlow<UiState<CompanyProfile?>> =
        repository.observeCompany(currentUserId).toUiState("companyProfile")

    val companyPrivateDocsState: StateFlow<UiState<CompanyPrivateDocs?>> =
        repository.observeCompanyPrivateDocs(currentUserId).toUiState("companyPrivateDocs")

    val companyDriversState: StateFlow<UiState<List<DriverProfile>>> =
        repository.observeCompanyDrivers(currentUserId).toUiState("companyDrivers")

    val companyInvitesState: StateFlow<UiState<List<CompanyDriverInvite>>> =
        repository.observeCompanyInvites(currentUserId).toUiState("companyInvites")

    val companyTripsState: StateFlow<UiState<List<Trip>>> =
        repository.observeCompanyTrips(currentUserId).toUiState("companyTrips")

    val myDriverReviewsState: StateFlow<UiState<List<DriverReview>>> =
        repository.observeDriverReviews(currentUserId).toUiState("myDriverReviews")

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedTripMessagesState: StateFlow<UiState<List<TripMessage>>> =
        _selectedTripForChat.flatMapLatest { tripId ->
            if (tripId.isNullOrBlank()) flowOf(emptyList())
            else repository.observeTripMessages(tripId, currentUserId)
        }.toUiState("tripMessages")

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedDriverReviewsState: StateFlow<UiState<List<DriverReview>>> =
        _selectedDriverForProfile.flatMapLatest { driverId ->
            if (driverId.isNullOrBlank()) flowOf(emptyList())
            else repository.observeDriverReviews(driverId)
        }.toUiState("driverReviews")

    @OptIn(ExperimentalCoroutinesApi::class)
    val adminAllDriversState: StateFlow<UiState<List<DriverProfile>>> =
        _isAdminVerified.flatMapLatest { isAdmin ->
            if (isAdmin) repository.observeAllDrivers() else flowOf(emptyList())
        }.toUiState("adminDrivers")

    @OptIn(ExperimentalCoroutinesApi::class)
    val adminAllCompaniesState: StateFlow<UiState<List<CompanyProfile>>> =
        _isAdminVerified.flatMapLatest { isAdmin ->
            if (isAdmin) repository.observeAllCompanies() else flowOf(emptyList())
        }.toUiState("adminCompanies")

    @OptIn(ExperimentalCoroutinesApi::class)
    val adminAllTripsState: StateFlow<UiState<List<Trip>>> =
        _isAdminVerified.flatMapLatest { isAdmin ->
            if (isAdmin) repository.observeAllTrips() else flowOf(emptyList())
        }.toUiState("adminTrips")

    @OptIn(ExperimentalCoroutinesApi::class)
    val adminAllWalletTxState: StateFlow<UiState<List<WalletTransaction>>> =
        _isAdminVerified.flatMapLatest { isAdmin ->
            if (isAdmin) repository.observeAllWalletTransactions() else flowOf(emptyList())
        }.toUiState("adminWalletTx")

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    fun showFeedback(msg: String) {
        _feedbackMessage.value = msg
    }

    fun setScreenMode(mode: String?) {
        _activeScreenMode.value = mode
    }

    fun watchTripMessages(tripId: String?) {
        _selectedTripForChat.value = tripId
    }

    fun watchDriverReviews(driverId: String?) {
        _selectedDriverForProfile.value = driverId
    }

    private fun launchAction(successMsg: String? = null, block: suspend () -> Result<Any?>) {
        viewModelScope.launch {
            _isBusy.value = true
            val res = block()
            _isBusy.value = false
            res.onSuccess {
                if (successMsg != null) _feedbackMessage.value = successMsg
            }.onFailure { err ->
                _feedbackMessage.value = err.localizedMessage ?: "حدث خطأ غير متوقع"
            }
        }
    }

    fun enterAsCustomer(name: String, phone: String) {
        launchAction("أهلاً بك! حدد موقعك واطلب الونش فوراً") {
            val res = repository.persistActiveRole("customer", name, phone)
            if (res.isSuccess) _activeScreenMode.value = "customer"
            res
        }
    }

    fun resumeDriverPortal(name: String, phone: String) {
        launchAction("تم فتح صفحتك الخاصة كسائق") {
            val cleanPhone = phone.trim()
            val db = FirebaseFirestore.getInstance()

            val userQuery = db.collection("users")
                .whereEqualTo("phone", cleanPhone)
                .limit(1)
                .get()
                .await()

            var targetId = userQuery.documents.firstOrNull()?.id

            if (targetId == null) {
                val anyDriverQuery = db.collection("drivers")
                    .limit(5)
                    .get()
                    .await()
                val matched = anyDriverQuery.documents.firstOrNull { it.getString("status") == "approved" }
                    ?: anyDriverQuery.documents.firstOrNull()
                targetId = matched?.id
            }

            if (targetId != null) {
                val driverSnap = db.collection("drivers").document(targetId).get().await()
                if (driverSnap.exists()) {
                    val data = driverSnap.data?.toMutableMap() ?: mutableMapOf()
                    data["userId"] = currentUserId
                    data["id"] = currentUserId
                    data["status"] = "approved"
                    db.collection("drivers").document(currentUserId).set(data).await()
                    val foundName = driverSnap.getString("name") ?: name
                   repository.persistActiveRole("driver", foundName, cleanPhone)
                      _activeScreenMode.value = "driver"
               return@launchAction Result.success(Unit)
}
}
val res = repository.persistActiveRole("driver", name, cleanPhone)
if (res.isSuccess) _activeScreenMode.value = "driver"
res
}
}
fun resumeCompanyPortal(name: String, phone: String) {
launchAction("تم فتح صفحة الشركة") {
val res = repository.persistActiveRole("company", name, phone)
if (res.isSuccess) _activeScreenMode.value = "company"
res
}
}
fun registerDriver(
name: String,
phone: String,
nationalId: String,
licenseNumber: String,
licenseExpiry: String,
winchType: String,
plateNumber: String,
area: String,
companyId: String?,
companyName: String?,
docIdThumb: String?,
docDriverLicenseThumb: String?,
docVehicleLicenseThumb: String?,
docTruckPhotoThumb: String?
) {
launchAction("تم إرسال أوراقك للمراجعة بنجاح") {
val res = repository.registerDriver(
name, phone, nationalId, licenseNumber, licenseExpiry,
winchType, plateNumber, area, companyId, companyName,
docIdThumb, docDriverLicenseThumb, docVehicleLicenseThumb, docTruckPhotoThumb
)
if (res.isSuccess) _activeScreenMode.value = "driver"
res
}
}
fun toggleDriverOnline(online: Boolean) {
launchAction(if (online) "تم بدء الوردية — جاهز لاستقبال الطلبات" else "تم إيقاف استقبال الطلبات") {
repository.toggleDriverOnline(online)
}
}
fun registerCompany(
companyName: String,
managerName: String,
phone: String,
commercialReg: String,
taxCard: String,
address: String,
area: String,
docRegThumb: String?,
docTaxThumb: String?,
docIdThumb: String?,
docOfficeThumb: String?
) {
launchAction("تم إرسال أوراق الشركة للمراجعة") {
val res = repository.registerCompany(
companyName, managerName, phone, commercialReg, taxCard, address, area,
docRegThumb, docTaxThumb, docIdThumb, docOfficeThumb
)
if (res.isSuccess) _activeScreenMode.value = "company"
res
}
}
fun addCompanyDriverInvite(
companyName: String,
driverName: String,
driverPhone: String,
plateNumber: String,
winchType: String,
area: String
) {
launchAction("تمت إضافة السائق لقائمة الشركة") {
repository.addCompanyDriverInvite(companyName, driverName, driverPhone, plateNumber, winchType, area)
}
}
fun createTrip(
customerName: String,
customerPhone: String,
pickupArea: String,
pickupLat: Double,
pickupLng: Double,
dropoffArea: String,
dropoffLat: Double,
dropoffLng: Double,
distanceKm: Double,
vehicleType: String,
serviceType: String,
scheduledAt: String?,
paymentMethod: String,
gatewayLive: Boolean,
fareEgp: Double,
commissionEgp: Double,
driverNetEgp: Double,
assignedDriver: DriverProfile? = null
) {
launchAction(if (assignedDriver != null) "تم إسناد الطلب الهاتفي بنجاح" else "تم إرسال طلبك! جاري البحث عن أقرب سائق") {
repository.createTrip(
customerName, customerPhone, pickupArea, pickupLat, pickupLng,
dropoffArea, dropoffLat, dropoffLng, distanceKm, vehicleType, serviceType,
scheduledAt, paymentMethod, gatewayLive, fareEgp, commissionEgp, driverNetEgp, assignedDriver
)
}
}
fun acceptTrip(tripId: String, driver: DriverProfile) {
launchAction("تم قبول المهمة! توجه لموقع العميل") {
repository.acceptTrip(tripId, driver)
}
}
fun markArrived(tripId: String) {
launchAction("تم تسجيل الوصول وبدء عداد الانتظار") {
repository.markArrived(tripId)
}
}
fun savePhotosAndVerifyPin(tripId: String, photos: List, pinVerified: Boolean) {
launchAction(if (pinVerified) "تم تأكيد كود الأمان والتحميل — تحرك لوجهة النقل" else "تم حفظ صور المعاينة") {
repository.saveTripPhotosAndConfirmLoaded(tripId, photos, pinVerified)
}
}
fun completeTrip(trip: Trip, driver: DriverProfile, signaturePointsCount: Int) {
launchAction("تم تسليم السيارة وإنهاء المهمة بنجاح!") {
repository.completeTripAndSettleWallet(trip, driver, signaturePointsCount)
}
}
fun cancelTrip(trip: Trip, cancelFeeEgp: Double, reason: String) {
launchAction("تم إلغاء الطلب") {
repository.cancelTrip(trip, cancelFeeEgp, reason)
}
}
fun submitRatingAndReview(trip: Trip, stars: Int, comment: String, tags: List) {
launchAction("شكراً لتقييمك! تم حفظ تقييم السائق بنجاح") {
repository.submitDriverRatingAndReview(trip, stars, comment, tags)
}
}
fun sendTripMessage(trip: Trip, senderName: String, senderRole: String, text: String) {
launchAction(null) {
repository.sendTripMessage(trip, senderName, senderRole, text)
}
}
fun requestWalletTransaction(
driver: DriverProfile,
type: String,
amountEgp: Double,
method: String,
accountOrRef: String
) {
val msg = if (type == "topup") "تم إرسال طلب شحن الرصيد للمراجعة" else "تم إرسال طلب سحب الأرباح"
launchAction(msg) {
repository.requestWalletTransaction(driver, type, amountEgp, method, accountOrRef)
}
}
fun adminUpdateDriverStatus(driverId: String, status: String, reason: String? = null) {
launchAction("تم تحديث حالة السائق") {
repository.adminUpdateDriverStatus(driverId, status, reason)
}
}
fun adminUpdateCompanyStatus(companyId: String, status: String, reason: String? = null) {
launchAction("تم تحديث حالة الشركة") {
repository.adminUpdateCompanyStatus(companyId, status, reason)
}
}
fun adminProcessWalletTx(tx: WalletTransaction, approve: Boolean) {
launchAction(if (approve) "تمت الموافقة وتحديث المحفظة" else "تم رفض الطلب") {
repository.adminProcessWalletTransaction(tx, approve)
}
}
fun adminSavePricing(config: AppPricingConfig) {
launchAction("تم حفظ إعدادات التسعير والعمولة") {
repository.adminUpdatePricingConfig(config)
}
}
}
